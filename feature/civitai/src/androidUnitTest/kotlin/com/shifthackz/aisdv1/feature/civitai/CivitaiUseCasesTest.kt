package com.shifthackz.aisdv1.feature.civitai

import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.domain.entity.CivitaiAuthorizationCancelled
import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthCode
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.domain.gateway.CivitaiOAuthBrowser
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository
import com.shifthackz.aisdv1.domain.usecase.civitai.UploadToCivitaiUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects public-sharing consent, durable duplicate guards and optional-upload recovery.
 *
 * @author Dmitriy Moroz
 */
class CivitaiUseCasesTest {
    @Test
    fun `given failed prior receipt, manual upload is retried, expected recorded output published once`() =
        runTest {
            // Given
            coEvery { repository.account() } returns CivitaiAccount(7, "creator")
            coEvery { repository.receipt(12) } returns receipt.copy(
                status = CivitaiUploadStatus.FAILED,
                url = null
            )
            coEvery { gallery.getById(12) } returns civitaiTestResult
            coEvery { repository.publish(civitaiTestResult, "description") } returns receipt
            // When
            val url = manual(12)
            // Then
            assertEquals(receipt.url, url)
            coVerify(exactly = 1) { repository.publish(civitaiTestResult, "description") }
        }

    @Test
    fun `given publication returns no URL, manual upload completes, expected invalid receipt rejected`() =
        runTest {
            // Given
            coEvery { repository.account() } returns CivitaiAccount(7, "creator")
            coEvery { repository.receipt(12) } returns null
            coEvery { gallery.getById(12) } returns civitaiTestResult
            coEvery { repository.publish(civitaiTestResult, "description") } returns receipt.copy(
                url = null
            )
            // When
            val failure = runCatching { manual(12) }.exceptionOrNull()
            // Then
            assertTrue(failure != null)
            coVerify(exactly = 1) { repository.publish(civitaiTestResult, "description") }
        }

    private val repository = mockk<CivitaiRepository>(relaxed = true)
    private val gallery = mockk<GenerationResultRepository>()
    private val description =
        mockk<CivitaiDescription> { every { this@mockk.invoke(any()) } returns "description" }
    private val receipt = CivitaiUpload(
        12,
        7,
        "https://civitai.com/posts/123",
        200,
        100,
        "revision",
        CivitaiUploadStatus.PUBLISHED
    )
    private val manual = UploadToCivitaiUseCaseImpl(repository, gallery, description)

    @Test
    fun `given manual upload while signed out, publication requested, expected sign-in failure before image loading`() =
        runTest {
            // Given
            coEvery { repository.account() } returns null
            // When
            val failure = runCatching { manual(12) }.exceptionOrNull()
            // Then
            assertEquals("CIVITAI_SIGN_IN_REQUIRED", failure?.message)
            coVerify(exactly = 0) { gallery.getById(any()) }
        }

    @Test
    fun `given invalid image identity, manual upload requested, expected rejection without account or transport calls`() =
        runTest {
            // Given
            val invalid = 0L
            // When
            val failure = runCatching { manual(invalid) }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.account() }
        }

    @Test
    fun `given published receipt, manual upload requested, expected existing URL returned without image read or repost`() =
        runTest {
            // Given
            coEvery { repository.account() } returns CivitaiAccount(7, "account")
            coEvery { repository.receipt(12) } returns receipt
            // When
            val url = manual(12)
            // Then
            assertEquals(receipt.url, url)
            coVerify(exactly = 0) { gallery.getById(any()) }
            coVerify(exactly = 0) { repository.publish(any(), any()) }
        }

    @Test
    fun `given uncertain publication, manual upload requested, expected duplicate guard prevents repost`() =
        runTest {
            // Given
            coEvery { repository.account() } returns CivitaiAccount(7, "account")
            coEvery { repository.receipt(12) } returns receipt.copy(
                url = null,
                status = CivitaiUploadStatus.UNCERTAIN
            )
            // When
            val failure = runCatching { manual(12) }.exceptionOrNull()
            // Then
            assertEquals("CIVITAI_PUBLICATION_UNCERTAIN", failure?.message)
            coVerify(exactly = 0) { repository.publish(any(), any()) }
        }

    @Test
    fun `given restored image with public URL and no receipt, manual upload requested, expected URL reused without repost`() =
        runTest {
            // Given
            coEvery { repository.account() } returns CivitaiAccount(7, "account")
            coEvery { repository.receipt(12) } returns null
            coEvery { gallery.getById(12) } returns civitaiTestResult.copy(civitaiUrl = receipt.url)
            // When
            val url = manual(12)
            // Then
            assertEquals(receipt.url, url)
            coVerify(exactly = 0) { repository.publish(any(), any()) }
        }

    @Test
    fun `given saved unpublished output, manual upload requested, expected original output passed to publication`() =
        runTest {
            // Given
            coEvery { repository.account() } returns CivitaiAccount(7, "account")
            coEvery { repository.receipt(12) } returns null
            coEvery { gallery.getById(12) } returns civitaiTestResult
            coEvery { repository.publish(civitaiTestResult, "description") } returns receipt
            // When
            val url = manual(12)
            // Then
            assertEquals(receipt.url, url)
            coVerify(exactly = 1) { repository.publish(civitaiTestResult, "description") }
        }

    @Test
    fun `given auto-upload disabled, generation completes, expected no save account read or publication`() =
        runTest {
            // Given
            coEvery { repository.isAutoUploadEnabled() } returns false
            val upload = mockk<UploadToCivitaiUseCase>()
            val useCase = AutoUploadToCivitaiUseCaseImpl(repository, gallery, upload)
            // When
            val result = useCase(civitaiTestResult)
            // Then
            assertSame(civitaiTestResult, result)
            coVerify(exactly = 0) { repository.account() }
            coVerify(exactly = 0) { upload(any()) }
        }

    @Test
    fun `given enabled auto-upload but no account, generation completes, expected output preserved without publication`() =
        runTest {
            // Given
            coEvery { repository.isAutoUploadEnabled() } returns true
            coEvery { repository.account() } returns null
            val upload = mockk<UploadToCivitaiUseCase>()
            // When
            val result =
                AutoUploadToCivitaiUseCaseImpl(repository, gallery, upload)(civitaiTestResult)
            // Then
            assertSame(civitaiTestResult, result)
            coVerify(exactly = 0) { upload(any()) }
        }

    @Test
    fun `given enabled auto-upload and unsaved output, publication fails, expected durable output identity retained`() =
        runTest {
            // Given
            coEvery { repository.isAutoUploadEnabled() } returns true
            coEvery { repository.account() } returns CivitaiAccount(7, "account")
            val unsaved = civitaiTestResult.copy(id = 0)
            coEvery { gallery.insert(unsaved) } returns 55
            val upload = mockk<UploadToCivitaiUseCase> {
                coEvery { this@mockk.invoke(55) } throws IllegalStateException("transport")
            }
            // When
            val result = AutoUploadToCivitaiUseCaseImpl(repository, gallery, upload)(unsaved)
            // Then
            assertEquals(unsaved.copy(id = 55), result)
            coVerify(exactly = 1) { gallery.insert(unsaved) }
        }

    @Test
    fun `given saved output and enabled auto-upload, generation completes, expected no duplicate save`() =
        runTest {
            // Given
            coEvery { repository.isAutoUploadEnabled() } returns true
            coEvery { repository.account() } returns CivitaiAccount(7, "account")
            val upload =
                mockk<UploadToCivitaiUseCase> { coEvery { this@mockk.invoke(12) } returns "url" }
            // When
            val result =
                AutoUploadToCivitaiUseCaseImpl(repository, gallery, upload)(civitaiTestResult)
            // Then
            assertSame(civitaiTestResult, result)
            coVerify(exactly = 0) { gallery.insert(any()) }
            coVerify(exactly = 1) { upload(12) }
        }

    @Test
    fun `given cancelled auto-upload, generation publication stops, expected cancellation propagated`() =
        runTest {
            // Given
            coEvery { repository.isAutoUploadEnabled() } throws CancellationException("cancel")
            // When
            val failure = runCatching {
                AutoUploadToCivitaiUseCaseImpl(repository, gallery, mockk())(civitaiTestResult)
            }.exceptionOrNull()
            // Then
            assertTrue(failure is CancellationException)
        }

    @Test
    fun `given validated native OAuth proof, sign-in completes, expected original proof exchanged`() =
        runTest {
            // Given
            val proof = CivitaiOAuthCode("code", "verifier")
            val browser = mockk<CivitaiOAuthBrowser> { coEvery { authorize() } returns proof }
            // When
            SignInToCivitaiUseCaseImpl(browser, repository)()
            // Then
            coVerify(exactly = 1) { repository.connectOAuth(proof) }
        }

    @Test
    fun `given native authorization cancellation, sign-in stops, expected existing credentials untouched`() =
        runTest {
            // Given
            val cancellation = CivitaiAuthorizationCancelled()
            val browser = mockk<CivitaiOAuthBrowser> { coEvery { authorize() } throws cancellation }
            // When
            val failure =
                runCatching { SignInToCivitaiUseCaseImpl(browser, repository)() }.exceptionOrNull()
            // Then
            assertSame(cancellation, failure)
            coVerify(exactly = 0) { repository.connectOAuth(any()) }
        }
}
