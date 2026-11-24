package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.core.navigation.router.UpscalerRouter
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.gateway.GenerationRestoreGate
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActor
import com.shifthackz.aisdv1.domain.gateway.UpscalerGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.GetRandomImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.FinalizeGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.UpscaleImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ReadUpscalerImageUseCase
import com.shifthackz.aisdv1.domain.entity.ProcessedImage
import com.shifthackz.aisdv1.domain.usecase.generation.GetUpscalerOutputSizeUseCase
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.io.encoding.Base64

/**
 * Checks input admission and shared progress/result state without replacing real-app inference QA.
 *
 * @param isAutoSaveEnabled Gallery policy captured in the completed result modal.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class UpscalerViewModelTest(private val isAutoSaveEnabled: Boolean) {
    private val dispatcher = StandardTestDispatcher()
    private val stores = mutableListOf<ViewModelStore>()
    private val random = mockk<GetRandomImageUseCase>()
    private val upscale = mockk<UpscaleImageUseCase>()
    private val finalize = mockk<FinalizeGenerationUseCase>()
    private val router = mockk<UpscalerRouter>(relaxed = true)
    private val readImage = mockk<ReadUpscalerImageUseCase>()
    private val preferences = mockk<PreferenceManager>()
    private val outputSize = mockk<GetUpscalerOutputSizeUseCase>()
    private val wakeLock = mockk<WakeLockInterActor>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { outputSize(any(), any(), any()) } returns null
        every { preferences.autoSaveAiResults } returns isAutoSaveEnabled
        every { readImage(any()) } answers { ProcessedImage(firstArg(), 512, 512) }
        coEvery { finalize(any()) } coAnswers {
            firstArg<AiGenerationResult>().copy(id = if (isAutoSaveEnabled) 42 else 0)
        }
    }

    @After
    fun tearDown() {
        stores.forEach(ViewModelStore::clear)
        Dispatchers.resetMain()
    }

    @Test
    fun `given random image acquisition, multiple clicks occur while loading, expected one fetch and displayed input state`() =
        runTest {
            // Given
            val bytes = byteArrayOf(1, 2, 3)
            val pending = CompletableDeferred<ByteArray>()
            coEvery { random() } coAnswers { pending.await() }
            val viewModel = createViewModel()

            // When
            viewModel.processIntent(UpscalerIntent.RandomImage)
            runCurrent()
            repeat(3) { viewModel.processIntent(UpscalerIntent.RandomImage) }
            val wasLoading = viewModel.state.value.isPickingImage
            pending.complete(bytes)
            advanceUntilIdle()

            // Then
            assertTrue(wasLoading)
            assertFalse(viewModel.state.value.isPickingImage)
            assertEquals(Base64.encode(bytes), viewModel.state.value.image)
            coVerify(exactly = 1) { random() }
        }

    @Test
    fun `given standalone upscaling, inference completes, expected shared progress then settings-aware result without automatic navigation`() =
        runTest {
            // Given
            val pending = CompletableDeferred<Unit>()
            coEvery { upscale(any(), any()) } coAnswers {
                pending.await()
                firstArg<AiGenerationResult>().copy(image = "enlarged", width = 2048, height = 2048)
            }
            val viewModel = createViewModel()
            viewModel.processIntent(UpscalerIntent.SelectImage("original"))
            advanceUntilIdle()
            viewModel.processIntent(UpscalerIntent.ConfigureModel(UpscalerConfiguration("model")))

            // When
            viewModel.processIntent(UpscalerIntent.Run)
            runCurrent()
            val progress = viewModel.state.value.screenModal
            pending.complete(Unit)
            advanceUntilIdle()

            // Then
            assertTrue(progress is GenerationModal.Generating)
            val result = viewModel.state.value.screenModal as GenerationModal.Image.Single
            assertEquals(isAutoSaveEnabled, result.autoSaveEnabled)
            assertEquals("enlarged", result.result.image)
            assertEquals(if (isAutoSaveEnabled) 42L else 0L, result.result.id)
            assertFalse(viewModel.state.value.isRunning)
            verify(exactly = 0) { router.navigateToGalleryDetails(any()) }
        }

    @Test
    fun `given oversized standalone input, upscale is pressed, expected warning with original landscape pixels and no inference`() =
        runTest {
            // Given
            every { readImage("landscape") } returns ProcessedImage("landscape", 2048, 1152)
            val viewModel = createViewModel()
            viewModel.processIntent(UpscalerIntent.SelectImage("landscape"))
            advanceUntilIdle()
            viewModel.processIntent(UpscalerIntent.ConfigureModel(UpscalerConfiguration("model")))

            // When
            viewModel.processIntent(UpscalerIntent.Run)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.state.value.hasDownscaleWarning)
            assertFalse(viewModel.state.value.isRunning)
            assertEquals("landscape", viewModel.state.value.image)
            assertEquals(2048, viewModel.state.value.width)
            assertEquals(1152, viewModel.state.value.height)
            coVerify(exactly = 0) { upscale(any(), any()) }
        }

    @Test
    fun `given oversized standalone input, warning is cancelled then stale continue arrives, expected original portrait retained without inference`() =
        runTest {
            // Given
            every { readImage("portrait") } returns ProcessedImage("portrait", 768, 1536)
            val viewModel = createViewModel()
            viewModel.processIntent(UpscalerIntent.SelectImage("portrait"))
            advanceUntilIdle()
            viewModel.processIntent(UpscalerIntent.ConfigureModel(UpscalerConfiguration("model")))
            viewModel.processIntent(UpscalerIntent.Run)

            // When
            viewModel.processIntent(UpscalerIntent.DismissModal)
            viewModel.processIntent(UpscalerIntent.ConfirmDownscale)
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.state.value.hasDownscaleWarning)
            assertFalse(viewModel.state.value.isRunning)
            assertEquals("portrait", viewModel.state.value.image)
            assertEquals(768, viewModel.state.value.width)
            assertEquals(1536, viewModel.state.value.height)
            coVerify(exactly = 0) { upscale(any(), any()) }
        }

    @Test
    fun `given oversized standalone input, warning is accepted twice, expected one upscale with original pixels for proportional preparation`() =
        runTest {
            // Given
            every { readImage("landscape") } returns ProcessedImage("landscape", 2048, 1152)
            coEvery { upscale(any(), any()) } coAnswers {
                firstArg<AiGenerationResult>().copy(image = "enlarged", width = 4096, height = 2304)
            }
            val viewModel = createViewModel()
            viewModel.processIntent(UpscalerIntent.SelectImage("landscape"))
            advanceUntilIdle()
            viewModel.processIntent(UpscalerIntent.ConfigureModel(UpscalerConfiguration("model")))
            viewModel.processIntent(UpscalerIntent.Run)

            // When
            viewModel.processIntent(UpscalerIntent.ConfirmDownscale)
            viewModel.processIntent(UpscalerIntent.ConfirmDownscale)
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.state.value.hasDownscaleWarning)
            assertFalse(viewModel.state.value.isRunning)
            coVerify(exactly = 1) {
                upscale(
                    match { it.image == "landscape" && it.width == 2048 && it.height == 1152 },
                    any()
                )
            }
            val modal = viewModel.state.value.screenModal as GenerationModal.Image.Single
            assertEquals(4096, modal.result.width)
            assertEquals(2304, modal.result.height)
        }


    @Test
    fun `given standalone upscaling, native inference fails, expected visible error and no finalization`() =
        runTest {
            // Given
            coEvery { upscale(any(), any()) } throws IllegalStateException("native failed")
            val viewModel = createViewModel()
            viewModel.processIntent(UpscalerIntent.SelectImage("original"))
            advanceUntilIdle()
            viewModel.processIntent(UpscalerIntent.ConfigureModel(UpscalerConfiguration("model")))
            // When
            viewModel.processIntent(UpscalerIntent.Run)
            advanceUntilIdle()
            // Then
            assertTrue(viewModel.state.value.screenModal is GenerationModal.Error)
            assertFalse(viewModel.state.value.isRunning)
            coVerify(exactly = 0) { finalize(any()) }
        }

    @Test
    fun `given standalone image admission, malformed source is selected, expected error without changing previous valid input`() =
        runTest {
            // Given
            val viewModel = createViewModel()
            viewModel.processIntent(UpscalerIntent.SelectImage("original"))
            advanceUntilIdle()
            every { readImage("invalid") } throws IllegalArgumentException("invalid image")
            // When
            viewModel.processIntent(UpscalerIntent.SelectImage("invalid"))
            advanceUntilIdle()
            // Then
            assertEquals("original", viewModel.state.value.image)
            assertTrue(viewModel.state.value.screenModal is GenerationModal.Error)
            assertFalse(viewModel.state.value.isPickingImage)
        }

    @Test
    fun `given standalone upscaling, input and configuration are incomplete, expected no inference or finalization`() =
        runTest {
            // Given
            val viewModel = createViewModel()
            // When
            viewModel.processIntent(UpscalerIntent.Run)
            advanceUntilIdle()
            // Then
            assertFalse(viewModel.state.value.isRunning)
            coVerify(exactly = 0) { upscale(any(), any()) }
            coVerify(exactly = 0) { finalize(any()) }
        }

    @Test
    fun `given standalone upscaling, active inference is cancelled, expected wake lock released without saving or showing an error`() =
        runTest {
            // Given
            coEvery { upscale(any(), any()) } coAnswers { awaitCancellation() }
            val viewModel = createViewModel()
            viewModel.processIntent(UpscalerIntent.SelectImage("original"))
            advanceUntilIdle()
            viewModel.processIntent(UpscalerIntent.ConfigureModel(UpscalerConfiguration("model")))
            viewModel.processIntent(UpscalerIntent.Run)
            runCurrent()
            assertTrue(viewModel.state.value.isRunning)
            // When
            viewModel.processIntent(UpscalerIntent.Cancel)
            advanceUntilIdle()
            // Then
            assertFalse(viewModel.state.value.isRunning)
            assertEquals(GenerationModal.None, viewModel.state.value.screenModal)
            assertEquals("original", viewModel.state.value.image)
            verify(exactly = 1) { wakeLock.releaseWakeLockUseCase() }
            coVerify(exactly = 0) { finalize(any()) }
        }

    /**
     * Creates a retained operation owner whose collectors are cleared after each test.
     *
     * @author Dmitriy Moroz
     */
    private fun createViewModel(): UpscalerViewModel {
        val gateway = mockk<UpscalerGateway> {
            every { observeStage() } returns flowOf(NativeGenerationStage.PREPARING)
        }
        val dispatchers = mockk<DispatchersProvider> {
            every { io } returns dispatcher
            every { ui } returns dispatcher
            every { immediate } returns dispatcher
        }
        val time = mockk<TimeProvider> { every { currentTimeMillis() } returns 1L }
        val gate = object : GenerationRestoreGate {
            override suspend fun <T> generating(block: suspend () -> T): T = block()
            override suspend fun <T> restoring(block: suspend () -> T): T = block()
        }
        return UpscalerViewModel(
            diagnostics = mockk(relaxed = true),
            dispatchers = dispatchers,
            readImage = readImage,
            outputSize = outputSize,
            upscale = upscale,
            finalize = finalize,
            save = mockk(relaxed = true),
            cache = mockk(relaxed = true),
            randomImage = random,
            time = time,
            restoreGate = gate,
            picker = mockk(),
            router = router,
            buildInfo = BuildInfoProvider.stub,
            gateway = gateway,
            preferences = preferences,
            wakeLock = wakeLock,
            platformServices = mockk(relaxed = true),
        ).also { viewModel ->
            stores += ViewModelStore().apply { put("upscaler", viewModel) }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "autosave={0}")
        fun policies(): List<Array<Boolean>> = listOf(arrayOf(false), arrayOf(true))
    }
}
