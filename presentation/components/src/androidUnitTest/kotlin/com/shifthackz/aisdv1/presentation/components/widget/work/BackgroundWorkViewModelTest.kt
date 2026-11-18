package com.shifthackz.aisdv1.presentation.components.widget.work

import io.mockk.mockk

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkStatus
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundWorkViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchersProvider = object : DispatchersProvider {
        override val io: CoroutineDispatcher = testDispatcher
        override val ui: CoroutineDispatcher = testDispatcher
        override val immediate: CoroutineDispatcher = testDispatcher
    }
    private val observer = TestBackgroundWorkObserver()

    @Before
    fun initialize() {
        Localization.setLanguageCode("en")
        observer.reset()
    }

    @Test
    fun `given background work, running background work, expected status visible`() =
        runTest(testDispatcher) {
            // Given

            observer.status = flowOf(
                BackgroundWorkStatus(
                    running = true,
                    statusTitle = "Running",
                    statusSubTitle = "Almost there",
                ),
            )

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                BackgroundWorkState(
                    visible = true,
                    running = true,
                    title = "Running",
                    subTitle = "Almost there",
                ),
                viewModel.state.value,
            )

        }

    @Test
    fun `given background work, background work success, expected finish title and preview payload without eager decoding`() =
        runTest(testDispatcher) {
            // Given

            observer.result = flowOf(
                BackgroundWorkResult.Success(
                    listOf(aiGenerationResult(image = "base64-result")),
                ),
            )

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                BackgroundWorkState(
                    visible = true,
                    dismissible = true,
                    title = Localization.string("notification_finish_title", languageCode = "en"),
                    imageBase64 = "base64-result",
                ),
                viewModel.state.value,
            )

        }

    @Test
    fun `given background work, background work success without preview image, expected finish title and dismiss action`() =
        runTest(testDispatcher) {
            // Given

            observer.result = flowOf(BackgroundWorkResult.Success(emptyList()))

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                BackgroundWorkState(
                    visible = true,
                    dismissible = true,
                    title = Localization.string("notification_finish_title", languageCode = "en"),
                ),
                viewModel.state.value,
            )

        }

    @Test
    fun `given background work, running background work with stale success result, expected loading state without old image`() =
        runTest(testDispatcher) {
            // Given

            observer.status = flowOf(
                BackgroundWorkStatus(
                    running = true,
                    statusTitle = "Running",
                    statusSubTitle = "Please wait",
                ),
            )
            observer.result = flowOf(
                BackgroundWorkResult.Success(
                    listOf(aiGenerationResult(image = "old-base64-result")),
                ),
            )

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                BackgroundWorkState(
                    visible = true,
                    running = true,
                    title = "Running",
                    subTitle = "Please wait",
                ),
                viewModel.state.value,
            )

        }

    @Test
    fun `given background work, dismiss intent, expected state hidden and observer result dismissed`() =
        runTest(testDispatcher) {
            // Given

            observer.result = flowOf(BackgroundWorkResult.Error(IllegalStateException()))
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.processIntent(BackgroundWorkIntent.Dismiss)

            // Then
            Assert.assertEquals(
                BackgroundWorkState(visible = false),
                viewModel.state.value,
            )
            Assert.assertTrue(observer.dismissed)

        }

    private fun TestScope.createViewModel() = BackgroundWorkViewModel(
        diagnostics = mockk(relaxed = true),
        dispatchersProvider = dispatchersProvider,
        backgroundWorkObserver = observer,
    )

    private fun aiGenerationResult(image: String) = AiGenerationResult(
        id = 1L,
        image = image,
        inputImage = "",
        createdAt = 0L,
        type = AiGenerationResult.Type.TEXT_TO_IMAGE,
        prompt = "",
        negativePrompt = "",
        width = 512,
        height = 512,
        samplingSteps = 20,
        cfgScale = 7f,
        restoreFaces = false,
        sampler = "",
        seed = "",
        subSeed = "",
        subSeedStrength = 0f,
        denoisingStrength = 0f,
        hidden = false,
    )

    private class TestBackgroundWorkObserver : BackgroundWorkObserver {
        var status: Flow<BackgroundWorkStatus> = flowOf(BackgroundWorkStatus(false, "", ""))
        var result: Flow<BackgroundWorkResult> = flowOf(BackgroundWorkResult.None)
        var dismissed = false

        fun reset() {
            status = flowOf(BackgroundWorkStatus(false, "", ""))
            result = flowOf(BackgroundWorkResult.None)
            dismissed = false
        }

        override fun observeStatus(): Flow<BackgroundWorkStatus> = status
        override fun observeResult(): Flow<BackgroundWorkResult> = result
        override fun dismissResult() {
            dismissed = true
        }

        override fun refreshStatus() = Unit
        override fun postStatusMessage(title: String, subTitle: String) = Unit
        override fun postSuccessSignal(result: List<AiGenerationResult>) = Unit
        override fun postCancelSignal() = Unit
        override fun postFailedSignal(t: Throwable) = Unit
        override fun hasActiveTasks(): Boolean = false
    }

}
