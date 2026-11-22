package com.shifthackz.aisdv1.presentation.civitai.upload

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.domain.usecase.caching.GetLastResultFromCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.civitai.UploadToCivitaiUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Protects the gallery action's captured identity, duplicate-tap guard and transient-result persistence.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CivitaiUploadViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val dispatchers = mockk<DispatchersProvider> {
        every { io } returns dispatcher
        every { immediate } returns dispatcher
    }
    private val repository = mockk<CivitaiRepository>()
    private val upload = mockk<UploadToCivitaiUseCase>()
    private val cachedResult = mockk<GetLastResultFromCacheUseCase>()
    private val save = mockk<SaveGenerationResultUseCase>()
    private val cache = mockk<SaveLastResultToCacheUseCase>()
    private val url = "https://civitai.com/posts/123"
    private val original = AiGenerationResult(
        id = 0, image = "output", inputImage = "input", createdAt = 100,
        type = AiGenerationResult.Type.TEXT_TO_IMAGE, prompt = "cat", negativePrompt = "blur",
        width = 512, height = 512, samplingSteps = 20, cfgScale = 7f, restoreFaces = false,
        sampler = "Euler", seed = "42", subSeed = "", subSeedStrength = 0f,
        denoisingStrength = 0f, hidden = false,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        coEvery { repository.account() } returns CivitaiAccount(7, "creator")
        coEvery { repository.receipt(any()) } returns null
        coEvery { upload(any()) } returns url
        coEvery { cachedResult() } returns original
        coEvery { save(original) } returns 12
        coEvery { cache(any()) } answers { firstArg() }
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given signed-out gallery action, upload is tapped, expected login prompt before image persistence`() =
        runTest(dispatcher) {
            // Given
            coEvery { repository.account() } returns null
            val vm = viewModel(0)
            // When
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            // Then
            assertEquals(CivitaiUploadPhase.LOGIN_REQUIRED, vm.state.value.phase)
            coVerify(exactly = 0) { save(any()) }
            coVerify(exactly = 0) { upload(any()) }
        }

    @Test
    fun `given login prompt, sign-in is chosen, expected settings navigation effect and idle action`() =
        runTest(dispatcher) {
            // Given
            coEvery { repository.account() } returns null
            val vm = viewModel(12)
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            // When
            vm.processIntent(CivitaiUploadIntent.SIGN_IN)
            advanceUntilIdle()
            // Then
            assertEquals(CivitaiUploadPhase.IDLE, vm.state.value.phase)
            assertEquals(CivitaiUploadEffect.SignIn, vm.effect.first())
        }

    @Test
    fun `given published receipt, gallery action is tapped, expected existing post opens without repost`() =
        runTest(dispatcher) {
            // Given
            coEvery { repository.receipt(12) } returns CivitaiUpload(
                12,
                7,
                url,
                200,
                100,
                "revision",
                CivitaiUploadStatus.PUBLISHED
            )
            val vm = viewModel(12)
            // When
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            // Then
            assertEquals(CivitaiUploadPhase.IDLE, vm.state.value.phase)
            assertEquals(CivitaiUploadEffect.OpenPost(url), vm.effect.first())
            coVerify(exactly = 0) { upload(any()) }
        }

    @Test
    fun `given saved gallery image, upload completes, expected success without saving or caching again`() =
        runTest(dispatcher) {
            // Given
            val vm = viewModel(12)
            // When
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            // Then
            assertEquals(CivitaiUploadPhase.SUCCESS, vm.state.value.phase)
            coVerify(exactly = 1) { upload(12) }
            coVerify(exactly = 0) { save(any()) }
            coVerify(exactly = 0) { cache(any()) }
        }

    @Test
    fun `given transient result, upload is requested, expected persisted identity cached before publication`() =
        runTest(dispatcher) {
            // Given
            val vm = viewModel(0)
            // When
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            // Then
            assertEquals(CivitaiUploadPhase.SUCCESS, vm.state.value.phase)
            coVerifyOrder {
                cachedResult(); save(original); cache(original.copy(id = 12)); repository.receipt(
                12
            ); upload(12)
            }
        }

    @Test
    fun `given transient output with failed publication, upload is retried, expected saved identity reused without duplicate image`() =
        runTest(dispatcher) {
            // Given
            val vm = viewModel(0)
            coEvery { upload(12) } throws IllegalStateException("offline")
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            assertEquals(CivitaiUploadPhase.FAILURE, vm.state.value.phase)
            coEvery { upload(12) } returns url
            // When
            vm.processIntent(CivitaiUploadIntent.DISMISS)
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            // Then
            assertEquals(CivitaiUploadPhase.SUCCESS, vm.state.value.phase)
            coVerify(exactly = 1) { save(original) }
            coVerify(exactly = 1) { cache(original.copy(id = 12)) }
            coVerify(exactly = 2) { upload(12) }
        }

    @Test
    fun `given pending publication, repeated taps and dismiss arrive, expected one operation and unchanged busy state`() =
        runTest(dispatcher) {
            // Given
            val completion = CompletableDeferred<String>()
            coEvery { upload(12) } coAnswers { completion.await() }
            val vm = viewModel(12)
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            runCurrent()
            // When
            repeat(3) { vm.processIntent(CivitaiUploadIntent.UPLOAD) }
            vm.processIntent(CivitaiUploadIntent.DISMISS)
            vm.processIntent(CivitaiUploadIntent.SIGN_IN)
            runCurrent()
            // Then
            assertEquals(CivitaiUploadPhase.COMMUNICATING, vm.state.value.phase)
            coVerify(exactly = 1) { upload(12) }
            completion.complete(url)
            advanceUntilIdle()
            assertEquals(CivitaiUploadPhase.SUCCESS, vm.state.value.phase)
        }

    @Test
    fun `given publication failure, dialog is dismissed, expected idle action available for recovery`() =
        runTest(dispatcher) {
            // Given
            coEvery { upload(12) } throws IllegalStateException("offline")
            val vm = viewModel(12)
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            assertEquals(CivitaiUploadPhase.FAILURE, vm.state.value.phase)
            // When
            vm.processIntent(CivitaiUploadIntent.DISMISS)
            // Then
            assertEquals(CivitaiUploadPhase.IDLE, vm.state.value.phase)
        }

    @Test
    fun `given transient persistence failure, upload is tapped, expected failure without cache or publication`() =
        runTest(dispatcher) {
            // Given
            coEvery { save(original) } throws IllegalStateException("storage full")
            val vm = viewModel(0)
            // When
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            // Then
            assertEquals(CivitaiUploadPhase.FAILURE, vm.state.value.phase)
            coVerify(exactly = 0) { cache(any()) }
            coVerify(exactly = 0) { upload(any()) }
        }

    @Test
    fun `given cancelled publication, upload stops, expected cancellation not shown as an ordinary error`() =
        runTest(dispatcher) {
            // Given
            coEvery { upload(12) } throws CancellationException("route cancelled")
            val vm = viewModel(12)
            // When
            vm.processIntent(CivitaiUploadIntent.UPLOAD)
            advanceUntilIdle()
            // Then
            assertTrue(vm.state.value.phase != CivitaiUploadPhase.FAILURE)
            coVerify(exactly = 1) { upload(12) }
        }

    /**
     * Captures a gallery identity with deterministic coroutine dispatchers and inert diagnostics.
     *
     * @param imageId Saved gallery identity, or zero for the retained transient result.
     * @author Dmitriy Moroz
     */
    private fun viewModel(imageId: Long) = CivitaiUploadViewModel(
        imageId, repository, upload, cachedResult, save, cache, dispatchers, mockk(relaxed = true)
    )
}
