package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.data.civitai.CivitaiCredentialStore
import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.domain.datasource.CivitaiRemoteDataSource
import com.shifthackz.aisdv1.domain.datasource.CivitaiUploadLocalDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthCode
import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthTokens
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultMetadataEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Protects validated session persistence, original-image receipt guards and publication cancellation.
 *
 * @author Dmitriy Moroz
 */
class CivitaiRepositoryTest {
    @Test
    fun `given gallery original metadata, receipt requested, expected timestamp and revision guard without account filter`() =
        runTest {
            // Given
            val metadata = GenerationResultMetadataEntity(
                result.id,
                result.imageRevision,
                result.createdAt,
                result.type.key,
                result.prompt,
                result.negativePrompt,
                result.width,
                result.height,
                result.samplingSteps,
                result.cfgScale,
                result.restoreFaces,
                result.sampler,
                result.seed,
                result.subSeed,
                result.subSeedStrength,
                result.denoisingStrength,
                result.hidden,
                result.liked,
                result.modelName,
            )
            coEvery { gallery.queryMetadata(listOf(12)) } returns listOf(metadata)
            coEvery { uploads.find(12, 100, "revision", null) } returns receipt
            // When
            val found = repository.receipt(12)
            // Then
            assertSame(receipt, found)
            coVerify(exactly = 1) { uploads.find(12, 100, "revision", null) }
        }

    @Test
    fun `given deleted gallery image, receipt requested, expected no guess using a reused identity`() =
        runTest {
            // Given
            coEvery { gallery.queryMetadata(listOf(12)) } returns emptyList()
            // When
            val found = repository.receipt(12)
            // Then
            assertNull(found)
            coVerify(exactly = 0) { uploads.find(any(), any(), any(), any()) }
        }

    private val credentials = mockk<CivitaiCredentialStore>(relaxed = true)
    private val preferences = mockk<KeyValueStore>(relaxed = true)
    private val remote = mockk<CivitaiRemoteDataSource>()
    private val gallery = mockk<GenerationResultDao>(relaxed = true)
    private val uploads = mockk<CivitaiUploadLocalDataSource>()
    private val time = mockk<TimeProvider> { every { currentTimeMillis() } returns 1000 }
    private val repository =
        CivitaiRepositoryImpl(credentials, preferences, remote, gallery, uploads, time)
    private var protectedRecord: String? =
        "{\"id\":7,\"username\":\"account\",\"key\":\"test-access\"}"
    private val saved = mutableListOf<CivitaiUpload>()
    private val result = AiGenerationResult(
        12,
        "output",
        "input",
        100,
        AiGenerationResult.Type.TEXT_TO_IMAGE,
        "prompt",
        "negative",
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
    private val receipt = CivitaiUpload(
        12,
        7,
        "https://civitai.com/posts/123",
        200,
        100,
        "revision",
        CivitaiUploadStatus.PUBLISHED
    )

    @Before
    fun setup() {
        every { credentials.read() } answers { protectedRecord }
        every { credentials.replace(any()) } answers { protectedRecord = firstArg() }
        coEvery { uploads.find(any(), any(), any(), any()) } returns null
        coEvery { uploads.save(any()) } answers { firstArg<CivitaiUpload>().also { saved += it } }
    }

    @Test
    fun `given access-only OAuth response, account connects, expected valid account without requiring refresh token`() =
        runTest {
            // Given
            coEvery {
                remote.exchange(
                    "code",
                    "verifier"
                )
            } returns CivitaiOAuthTokens("test-access", null, 3600)
            coEvery { remote.account("test-access") } returns CivitaiAccount(7, "account")
            // When
            repository.connectOAuth(CivitaiOAuthCode("code", "verifier"))
            // Then
            assertEquals(CivitaiAccount(7, "account"), repository.account())
            assertTrue(!protectedRecord!!.contains("refresh"))
            verify {
                preferences.putSettingsDurably(
                    emptyMap(),
                    mapOf("civitai_auto_upload" to false),
                    emptyMap()
                )
            }
        }

    @Test
    fun `given expired access-only session, account is read, expected signed-out state without token leakage`() =
        runTest {
            // Given
            protectedRecord =
                "{\"id\":7,\"username\":\"account\",\"key\":\"test-access\",\"expiresAt\":1000}"
            // When
            val account = repository.account()
            // Then
            assertNull(account)
            coVerify(exactly = 0) { remote.refresh(any()) }
        }

    @Test
    fun `given account validation failure, new OAuth session connects, expected previous credentials retained`() =
        runTest {
            // Given
            val previous = protectedRecord
            val error = IllegalStateException("profile unavailable")
            coEvery { remote.exchange(any(), any()) } returns CivitaiOAuthTokens(
                "new-access",
                null,
                3600
            )
            coEvery { remote.account("new-access") } throws error
            // When
            val failure = runCatching {
                repository.connectOAuth(
                    CivitaiOAuthCode(
                        "code",
                        "verifier"
                    )
                )
            }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            assertEquals(previous, protectedRecord)
        }

    @Test
    fun `given malformed protected storage, account is read, expected storage failure rather than signed-out success`() =
        runTest {
            // Given
            protectedRecord = "invalid json"
            // When
            val failure = runCatching { repository.account() }.exceptionOrNull()
            // Then
            assertTrue(failure != null)
        }

    @Test
    fun `given existing published receipt, publication requested, expected no transport call or new receipt`() =
        runTest {
            // Given
            coEvery { uploads.find(12, 100, "revision", null) } returns receipt
            // When
            val found = repository.publish(result, "description")
            // Then
            assertSame(receipt, found)
            assertTrue(saved.isEmpty())
            coVerify(exactly = 0) { remote.publish(any(), any(), any(), any()) }
        }

    @Test
    fun `given uncertain receipt, publication requested, expected durable duplicate guard prevents transport`() =
        runTest {
            // Given
            coEvery {
                uploads.find(
                    12,
                    100,
                    "revision",
                    null
                )
            } returns receipt.copy(status = CivitaiUploadStatus.UNCERTAIN, url = null)
            // When
            val failure =
                runCatching { repository.publish(result, "description") }.exceptionOrNull()
            // Then
            assertEquals("CIVITAI_PUBLICATION_UNCERTAIN", failure?.message)
            coVerify(exactly = 0) { remote.publish(any(), any(), any(), any()) }
        }

    @Test
    fun `given server publishes an image, request completes, expected uncertain fence then published receipt before gallery URL`() =
        runTest {
            // Given
            coEvery { remote.publish(any(), result, "description", any()) } coAnswers {
                arg<suspend () -> Unit>(3)()
                assertEquals(CivitaiUploadStatus.UNCERTAIN, saved.last().status)
                receipt.url!!
            }
            coEvery { gallery.setCivitaiUrl(12, receipt.url!!) } coAnswers {
                assertEquals(CivitaiUploadStatus.PUBLISHED, saved.last().status)
            }
            // When
            val published = repository.publish(result, "description")
            // Then
            assertEquals(receipt.url, published.url)
            assertEquals(
                listOf(
                    CivitaiUploadStatus.UPLOADING,
                    CivitaiUploadStatus.UNCERTAIN,
                    CivitaiUploadStatus.PUBLISHED
                ), saved.map { it.status })
        }

    @Test
    fun `given failure before post submission, publication fails, expected failed receipt and original failure propagated`() =
        runTest {
            // Given
            val error = IllegalStateException("presign failed")
            coEvery { remote.publish(any(), any(), any(), any()) } throws error
            // When
            val failure =
                runCatching { repository.publish(result, "description") }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            assertEquals(CivitaiUploadStatus.FAILED, saved.last().status)
        }

    @Test
    fun `given ambiguous failure after submission fence, publication fails, expected uncertain receipt preserved`() =
        runTest {
            // Given
            val error = IllegalStateException("response lost")
            coEvery {
                remote.publish(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } coAnswers { arg<suspend () -> Unit>(3)(); throw error }
            // When
            val failure =
                runCatching { repository.publish(result, "description") }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            assertEquals(CivitaiUploadStatus.UNCERTAIN, saved.last().status)
            coVerify(exactly = 0) { gallery.setCivitaiUrl(any(), any()) }
        }

    @Test
    fun `given cancellation after post submission, publication stops, expected uncertain receipt and cancellation retained`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancel")
            coEvery {
                remote.publish(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } coAnswers { arg<suspend () -> Unit>(3)(); throw cancellation }
            // When
            val failure =
                runCatching { repository.publish(result, "description") }.exceptionOrNull()
            // Then
            assertSame(cancellation, failure)
            assertEquals(CivitaiUploadStatus.UNCERTAIN, saved.last().status)
        }

    @Test
    fun `given protected persistence failure, account replacement requested, expected failure propagated`() =
        runTest {
            // Given
            val error = IllegalStateException("keystore")
            coEvery { remote.account("new-key") } returns CivitaiAccount(8, "other")
            every { credentials.replace(any()) } throws error
            // When
            val failure = runCatching { repository.connect("new-key") }.exceptionOrNull()
            // Then
            assertSame(error, failure)
        }

    @Test
    fun `given API key account, disconnect requested, expected local credentials removed and history untouched`() =
        runTest {
            // Given
            val previous = protectedRecord
            // When
            repository.disconnect()
            // Then
            assertTrue(previous != null)
            assertNull(protectedRecord)
            coVerify(exactly = 0) { uploads.save(any()) }
            verify {
                preferences.putSettingsDurably(
                    emptyMap(),
                    mapOf("civitai_auto_upload" to false),
                    emptyMap()
                )
            }
        }

    @Test
    fun `given expiring renewable session, publication requested, expected rotated credentials committed before upload`() =
        runTest {
            // Given
            protectedRecord =
                "{\"id\":7,\"username\":\"account\",\"key\":\"old-access\",\"refresh\":\"old-refresh\",\"expiresAt\":1000}"
            coEvery { remote.refresh("old-refresh") } returns CivitaiOAuthTokens(
                "new-access",
                "new-refresh",
                3600
            )
            coEvery { remote.publish("new-access", any(), any(), any()) } coAnswers {
                assertTrue(protectedRecord!!.contains("new-refresh"))
                receipt.url!!
            }
            // When
            repository.publish(result, "description")
            // Then
            coVerify(exactly = 1) { remote.refresh("old-refresh") }
            assertTrue(protectedRecord!!.contains("new-access"))
        }
}
