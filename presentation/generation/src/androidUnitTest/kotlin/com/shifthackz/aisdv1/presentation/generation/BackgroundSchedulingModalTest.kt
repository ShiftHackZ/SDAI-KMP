package com.shifthackz.aisdv1.presentation.generation

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.validation.ValidationResult
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageActionHandler
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageState
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageActionHandler
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageState
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Exercises both action handlers through validated background submission on each platform.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundSchedulingModalTest {

    @Test
    fun `given IOS text generation, background submission succeeds, expected no scheduled modal`() =
        runTest {
            // Given
            val dispatchers = mockk<DispatchersProvider> {
                every { io } returns StandardTestDispatcher(testScheduler)
                every { immediate } returns StandardTestDispatcher(testScheduler)
            }
            val preferences = mockk<PreferenceManager>(relaxed = true) {
                every { backgroundGeneration } returns true
            }
            val manager = mockk<BackgroundTaskManager>(relaxed = true)
            val observer = mockk<BackgroundWorkObserver>(relaxed = true)
            val build = mockk<BuildInfoProvider> { every { platform } returns Platform.IOS }
            var state = TextToImageState(
                platform = Platform.IOS, mode = ServerSource.EASY_DIFFUSION,
                prompt = "cat", width = "512", height = "512"
            )
            val handler = TextToImageActionHandler(
                openAiOAuthGateway = mockk(relaxed = true),
                providerSelectionGuard = mockk(relaxed = true),
                dispatchersProvider = dispatchers,
                textToImageUseCase = mockk(relaxed = true),
                saveGenerationResultUseCase = mockk(relaxed = true),
                saveLastResultToCacheUseCase = mockk(relaxed = true),
                interruptSourceGenerationUseCase = mockk(relaxed = true),
                preferenceManager = preferences,
                backgroundTaskManager = manager,
                backgroundWorkObserver = observer,
                wakeLockInterActor = mockk(relaxed = true),
                platformServices = mockk(relaxed = true) { every { supportsBackgroundGeneration } returns true },
                buildInfoProvider = build,
                dimensionValidator = DimensionValidator { ValidationResult(true) },
                evaluateGenerationBenchmarkUseCase = { error("Remote work must not run the local benchmark gate") },
                recordBenchmarkPromptAnswerUseCase = { mockk() },
                suppressBenchmarkWarningsUseCase = { mockk() },
                imageSaver = mockk(relaxed = true),
                imageSharer = mockk(relaxed = true),
                router = mockk(relaxed = true),
                currentState = { state },
                emitState = { state = it },
                updateState = { state = it(state) },
                launch = { dispatcher, start, block -> launch(dispatcher, start, block) },
                onError = { throw AssertionError("Background submission failed", it) },
            )
            // When
            handler.generate()
            runCurrent()
            // Then
            verify(exactly = 1) { manager.scheduleTextToImageTask(match { it.prompt == "cat" }) }
            assertFalse(state.generating)
            assertEquals(GenerationModal.None, state.screenModal)
        }

    @Test
    fun `given ANDROID text generation, background submission succeeds, expected Android scheduled modal`() =
        runTest {
            // Given
            val dispatchers = mockk<DispatchersProvider> {
                every { io } returns StandardTestDispatcher(testScheduler)
                every { immediate } returns StandardTestDispatcher(testScheduler)
            }
            val preferences = mockk<PreferenceManager>(relaxed = true) {
                every { backgroundGeneration } returns true
            }
            val manager = mockk<BackgroundTaskManager>(relaxed = true)
            val observer = mockk<BackgroundWorkObserver>(relaxed = true)
            val build = mockk<BuildInfoProvider> { every { platform } returns Platform.ANDROID }
            var state = TextToImageState(
                platform = Platform.ANDROID, mode = ServerSource.EASY_DIFFUSION,
                prompt = "cat", width = "512", height = "512"
            )
            val handler = TextToImageActionHandler(
                openAiOAuthGateway = mockk(relaxed = true),
                providerSelectionGuard = mockk(relaxed = true),
                dispatchersProvider = dispatchers,
                textToImageUseCase = mockk(relaxed = true),
                saveGenerationResultUseCase = mockk(relaxed = true),
                saveLastResultToCacheUseCase = mockk(relaxed = true),
                interruptSourceGenerationUseCase = mockk(relaxed = true),
                preferenceManager = preferences,
                backgroundTaskManager = manager,
                backgroundWorkObserver = observer,
                wakeLockInterActor = mockk(relaxed = true),
                platformServices = mockk(relaxed = true) { every { supportsBackgroundGeneration } returns true },
                buildInfoProvider = build,
                dimensionValidator = DimensionValidator { ValidationResult(true) },
                evaluateGenerationBenchmarkUseCase = { error("Remote work must not run the local benchmark gate") },
                recordBenchmarkPromptAnswerUseCase = { mockk() },
                suppressBenchmarkWarningsUseCase = { mockk() },
                imageSaver = mockk(relaxed = true),
                imageSharer = mockk(relaxed = true),
                router = mockk(relaxed = true),
                currentState = { state },
                emitState = { state = it },
                updateState = { state = it(state) },
                launch = { dispatcher, start, block -> launch(dispatcher, start, block) },
                onError = { throw AssertionError("Background submission failed", it) },
            )
            // When
            handler.generate()
            runCurrent()
            // Then
            verify(exactly = 1) { manager.scheduleTextToImageTask(match { it.prompt == "cat" }) }
            assertFalse(state.generating)
            assertEquals(GenerationModal.Background.Scheduled, state.screenModal)
        }

    @Test
    fun `given IOS image generation, background submission succeeds, expected no scheduled modal`() =
        runTest {
            // Given
            val dispatchers = mockk<DispatchersProvider> {
                every { io } returns StandardTestDispatcher(testScheduler)
                every { immediate } returns StandardTestDispatcher(testScheduler)
            }
            val preferences = mockk<PreferenceManager>(relaxed = true) {
                every { backgroundGeneration } returns true
            }
            val manager = mockk<BackgroundTaskManager>(relaxed = true)
            val observer = mockk<BackgroundWorkObserver>(relaxed = true)
            val build = mockk<BuildInfoProvider> { every { platform } returns Platform.IOS }
            var state = ImageToImageState(
                platform = Platform.IOS, mode = ServerSource.EASY_DIFFUSION,
                prompt = "cat", width = "512", height = "512", imageBase64 = "input"
            )
            val handler = ImageToImageActionHandler(
                dispatchersProvider = dispatchers,
                originalImageBudget = mockk(relaxed = true),
                getRandomImageUseCase = mockk(relaxed = true),
                imageToImageUseCase = mockk(relaxed = true),
                saveGenerationResultUseCase = mockk(relaxed = true),
                saveLastResultToCacheUseCase = mockk(relaxed = true),
                interruptSourceGenerationUseCase = mockk(relaxed = true),
                preferenceManager = preferences,
                backgroundTaskManager = manager,
                backgroundWorkObserver = observer,
                wakeLockInterActor = mockk(relaxed = true),
                platformServices = mockk(relaxed = true) { every { supportsBackgroundGeneration } returns true },
                buildInfoProvider = build,
                dimensionValidator = DimensionValidator { ValidationResult(true) },
                evaluateGenerationBenchmarkUseCase = { error("Remote work must not run the local benchmark gate") },
                recordBenchmarkPromptAnswerUseCase = { mockk() },
                suppressBenchmarkWarningsUseCase = { mockk() },
                imageSaver = mockk(relaxed = true),
                imageSharer = mockk(relaxed = true),
                router = mockk(relaxed = true),
                platformActions = mockk(relaxed = true),
                currentState = { state },
                emitState = { state = it },
                updateState = { state = it(state) },
                launch = { dispatcher, start, block -> launch(dispatcher, start, block) },
                onError = { throw AssertionError("Background submission failed", it) },
            )
            // When
            handler.generate()
            runCurrent()
            // Then
            verify(exactly = 1) { manager.scheduleImageToImageTask(match { it.prompt == "cat" }) }
            assertFalse(state.generating)
            assertEquals(GenerationModal.None, state.screenModal)
        }

    @Test
    fun `given ANDROID image generation, background submission succeeds, expected Android scheduled modal`() =
        runTest {
            // Given
            val dispatchers = mockk<DispatchersProvider> {
                every { io } returns StandardTestDispatcher(testScheduler)
                every { immediate } returns StandardTestDispatcher(testScheduler)
            }
            val preferences = mockk<PreferenceManager>(relaxed = true) {
                every { backgroundGeneration } returns true
            }
            val manager = mockk<BackgroundTaskManager>(relaxed = true)
            val observer = mockk<BackgroundWorkObserver>(relaxed = true)
            val build = mockk<BuildInfoProvider> { every { platform } returns Platform.ANDROID }
            var state = ImageToImageState(
                platform = Platform.ANDROID, mode = ServerSource.EASY_DIFFUSION,
                prompt = "cat", width = "512", height = "512", imageBase64 = "input"
            )
            val handler = ImageToImageActionHandler(
                dispatchersProvider = dispatchers,
                originalImageBudget = mockk(relaxed = true),
                getRandomImageUseCase = mockk(relaxed = true),
                imageToImageUseCase = mockk(relaxed = true),
                saveGenerationResultUseCase = mockk(relaxed = true),
                saveLastResultToCacheUseCase = mockk(relaxed = true),
                interruptSourceGenerationUseCase = mockk(relaxed = true),
                preferenceManager = preferences,
                backgroundTaskManager = manager,
                backgroundWorkObserver = observer,
                wakeLockInterActor = mockk(relaxed = true),
                platformServices = mockk(relaxed = true) { every { supportsBackgroundGeneration } returns true },
                buildInfoProvider = build,
                dimensionValidator = DimensionValidator { ValidationResult(true) },
                evaluateGenerationBenchmarkUseCase = { error("Remote work must not run the local benchmark gate") },
                recordBenchmarkPromptAnswerUseCase = { mockk() },
                suppressBenchmarkWarningsUseCase = { mockk() },
                imageSaver = mockk(relaxed = true),
                imageSharer = mockk(relaxed = true),
                router = mockk(relaxed = true),
                platformActions = mockk(relaxed = true),
                currentState = { state },
                emitState = { state = it },
                updateState = { state = it(state) },
                launch = { dispatcher, start, block -> launch(dispatcher, start, block) },
                onError = { throw AssertionError("Background submission failed", it) },
            )
            // When
            handler.generate()
            runCurrent()
            // Then
            verify(exactly = 1) { manager.scheduleImageToImageTask(match { it.prompt == "cat" }) }
            assertFalse(state.generating)
            assertEquals(GenerationModal.Background.Scheduled, state.screenModal)
        }
}
