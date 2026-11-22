package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.data.civitai.CivitaiCredentialStore
import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.domain.datasource.CivitaiRemoteDataSource
import com.shifthackz.aisdv1.domain.datasource.CivitaiUploadLocalDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthTokens
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Protects durable authorization changes and local recovery when publication persistence fails.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CivitaiRepositoryRecoveryTest {
    private val credentials = mockk<CivitaiCredentialStore>()
    private val preferences = mockk<KeyValueStore>(relaxed = true)
    private val remote = mockk<CivitaiRemoteDataSource>()
    private val gallery = mockk<GenerationResultDao>(relaxed = true)
    private val uploads = mockk<CivitaiUploadLocalDataSource>()
    private val time = mockk<TimeProvider> { every { currentTimeMillis() } returns 1000L }
    private val repository =
        CivitaiRepositoryImpl(credentials, preferences, remote, gallery, uploads, time)
    private var protectedRecord: String? = """{"id":7,"username":"creator","key":"access"}"""
    private val saved = mutableListOf<CivitaiUpload>()
    private val url = "https://civitai.com/posts/123"
    private val result = AiGenerationResult(
        12,
        "output",
        "input",
        100,
        AiGenerationResult.Type.TEXT_TO_IMAGE,
        "cat",
        "blur",
        512,
        512,
        20,
        7f,
        false,
        "Euler",
        "42",
        "",
        0f,
        0f,
        false,
        imageRevision = "revision"
    )

    @Before
    fun setup() {
        every { credentials.read() } answers { protectedRecord }
        every { credentials.replace(any()) } answers { protectedRecord = firstArg() }
        coEvery { uploads.find(any(), any(), any(), any()) } answers { saved.lastOrNull() }
        coEvery { uploads.save(any()) } answers { firstArg<CivitaiUpload>().also { saved += it } }
        coEvery {
            remote.publish(
                any(),
                any(),
                any(),
                any()
            )
        } coAnswers { arg<suspend () -> Unit>(3)(); url }
    }

    @Test
    fun `given invalid API key input, connection requested, expected rejection before remote validation`() =
        runTest {
            // Given
            for (key in listOf("", "   ", "key with space", "key\nvalue")) {
                // When
                val failure = runCatching { repository.connect(key) }.exceptionOrNull()
                // Then
                assertTrue(failure is IllegalArgumentException)
            }
            coVerify(exactly = 0) { remote.account(any()) }
            verify(exactly = 0) { credentials.replace(any()) }
        }

    @Test
    fun `given signed-out account, automatic publishing enabled, expected rejection without persisted consent`() =
        runTest {
            // Given
            protectedRecord = null
            // When
            val failure = runCatching { repository.setAutoUploadEnabled(true) }.exceptionOrNull()
            // Then
            assertTrue(failure != null)
            verify(exactly = 0) { preferences.putSettingsDurably(any(), any(), any()) }
        }

    @Test
    fun `given signed-out account, automatic publishing disabled, expected durable disabled preference`() =
        runTest {
            // Given
            protectedRecord = null
            every { preferences.getBoolean("civitai_auto_upload") } returns false
            // When
            repository.setAutoUploadEnabled(false)
            val isEnabled = repository.isAutoUploadEnabled()
            // Then
            assertFalse(isEnabled)
            verify {
                preferences.putSettingsDurably(
                    emptyMap(),
                    mapOf("civitai_auto_upload" to false),
                    emptyMap()
                )
            }
        }

    @Test
    fun `given connected account, automatic publishing enabled, expected durable explicit consent`() =
        runTest {
            // Given
            every { preferences.getBoolean("civitai_auto_upload") } returns true
            // When
            repository.setAutoUploadEnabled(true)
            val isEnabled = repository.isAutoUploadEnabled()
            // Then
            assertTrue(isEnabled)
            verify {
                preferences.putSettingsDurably(
                    emptyMap(),
                    mapOf("civitai_auto_upload" to true),
                    emptyMap()
                )
            }
        }

    @Test
    fun `given durable consent write failure, new account connects, expected old protected account retained`() =
        runTest {
            // Given
            val previous = protectedRecord
            val error = IllegalStateException("preferences unavailable")
            coEvery { remote.account("new-key") } returns CivitaiAccount(8, "other")
            every { preferences.putSettingsDurably(any(), any(), any()) } throws error
            // When
            val failure = runCatching { repository.connect("new-key") }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            assertEquals(previous, protectedRecord)
            verify(exactly = 0) { credentials.replace(any()) }
        }

    @Test
    fun `given renewable account, disconnect succeeds, expected revocation before local session removal`() =
        runTest {
            // Given
            protectedRecord = """{"id":7,"username":"creator","key":"access","refresh":"refresh"}"""
            coEvery { remote.revoke("refresh") } coAnswers { assertTrue(protectedRecord != null) }
            // When
            repository.disconnect()
            // Then
            coVerify(exactly = 1) { remote.revoke("refresh") }
            assertNull(protectedRecord)
            assertTrue(saved.isEmpty())
        }

    @Test
    fun `given revocation failure, disconnect is requested, expected original failure and protected session retained`() =
        runTest {
            // Given
            protectedRecord = """{"id":7,"username":"creator","key":"access","refresh":"refresh"}"""
            val previous = protectedRecord
            val error = IllegalStateException("revocation unavailable")
            coEvery { remote.revoke("refresh") } throws error
            // When
            val failure = runCatching { repository.disconnect() }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            assertEquals(previous, protectedRecord)
        }

    @Test
    fun `given expired access-only account, publication requested, expected reauthorization before receipt creation`() =
        runTest {
            // Given
            protectedRecord = """{"id":7,"username":"creator","key":"access","expiresAt":1000}"""
            // When
            val failure =
                runCatching { repository.publish(result, "description") }.exceptionOrNull()
            // Then
            assertEquals("CIVITAI_SIGN_IN_REQUIRED", failure?.message)
            assertTrue(saved.isEmpty())
            coVerify(exactly = 0) { remote.publish(any(), any(), any(), any()) }
        }

    @Test
    fun `given rotating session with access-only response, publication requested, expected old refresh removed before API use`() =
        runTest {
            // Given
            protectedRecord =
                """{"id":7,"username":"creator","key":"access","refresh":"old","expiresAt":1000}"""
            coEvery { remote.refresh("old") } returns CivitaiOAuthTokens("new", null, 3600)
            coEvery { remote.publish("new", any(), any(), any()) } coAnswers {
                assertFalse(protectedRecord!!.contains("refresh"))
                assertTrue(protectedRecord!!.contains("new"))
                arg<suspend () -> Unit>(3)()
                url
            }
            // When
            val published = repository.publish(result, "description")
            // Then
            assertEquals(url, published.url)
        }

    @Test
    fun `given rotated credential commit failure, publication requested, expected no receipt or remote publication`() =
        runTest {
            // Given
            protectedRecord =
                """{"id":7,"username":"creator","key":"access","refresh":"old","expiresAt":1000}"""
            coEvery { remote.refresh("old") } returns CivitaiOAuthTokens("new", "rotated", 3600)
            val error = IllegalStateException("keystore unavailable")
            every { credentials.replace(any()) } throws error
            // When
            val failure =
                runCatching { repository.publish(result, "description") }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            assertTrue(saved.isEmpty())
            coVerify(exactly = 0) { remote.publish(any(), any(), any(), any()) }
        }

    @Test
    fun `given initial receipt commit failure, publication requested, expected no remote mutation`() =
        runTest {
            // Given
            val error = IllegalStateException("database unavailable")
            coEvery { uploads.save(any()) } throws error
            // When
            val failure =
                runCatching { repository.publish(result, "description") }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            coVerify(exactly = 0) { remote.publish(any(), any(), any(), any()) }
        }

    @Test
    fun `given upload failure and failed cleanup write, publication fails, expected primary error with suppressed storage failure`() =
        runTest {
            // Given
            val primary = IllegalStateException("upload failed")
            val cleanup = IllegalStateException("storage failed")
            coEvery { remote.publish(any(), any(), any(), any()) } throws primary
            coEvery { uploads.save(match { it.status == CivitaiUploadStatus.FAILED }) } throws cleanup
            // When
            val failure =
                runCatching { repository.publish(result, "description") }.exceptionOrNull()
            // Then
            assertSame(primary, failure)
            assertTrue(primary.suppressed.any { suppressed ->
                generateSequence(suppressed) { it.cause }.any { it === cleanup }
            })
        }

    @Test
    fun `given published receipt and gallery decoration failure, publication is retried, expected saved receipt prevents repost`() =
        runTest {
            // Given
            val error = IllegalStateException("gallery metadata write failed")
            coEvery { gallery.setCivitaiUrl(any(), any()) } throws error
            val failure =
                runCatching { repository.publish(result, "description") }.exceptionOrNull()
            assertTrue(generateSequence(failure) { it.cause }.any { it === error })
            assertEquals(CivitaiUploadStatus.PUBLISHED, saved.last().status)
            // When
            val recovered = repository.publish(result, "description")
            // Then
            assertEquals(url, recovered.url)
            coVerify(exactly = 1) { remote.publish(any(), any(), any(), any()) }
        }

    @Test
    fun `given concurrent publication requests, first request completes, expected one post and shared durable receipt`() =
        runTest {
            // Given
            val completion = CompletableDeferred<Unit>()
            coEvery { remote.publish(any(), any(), any(), any()) } coAnswers {
                completion.await()
                arg<suspend () -> Unit>(3)()
                url
            }
            val first = async { repository.publish(result, "description") }
            runCurrent()
            // When
            val second = async { repository.publish(result, "description") }
            runCurrent()
            completion.complete(Unit)
            // Then
            assertEquals(first.await(), second.await())
            coVerify(exactly = 1) { remote.publish(any(), any(), any(), any()) }
        }
}
