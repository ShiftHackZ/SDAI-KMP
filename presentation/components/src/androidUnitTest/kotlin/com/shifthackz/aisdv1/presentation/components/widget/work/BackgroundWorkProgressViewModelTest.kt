package com.shifthackz.aisdv1.presentation.components.widget.work

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkStatus
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies measured progress reaches the card and cannot survive queue, terminal or dismissal transitions.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundWorkProgressViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val status = MutableStateFlow(BackgroundWorkStatus(true, "Running", "Denoising", 25))
    private val result = MutableStateFlow<BackgroundWorkResult>(BackgroundWorkResult.None)
    private val observer = mockk<BackgroundWorkObserver>(relaxed = true) {
        every { observeStatus() } returns status
        every { observeResult() } returns result
    }
    private val dispatchers = object : DispatchersProvider {
        override val io = dispatcher
        override val ui = dispatcher
        override val immediate = dispatcher
    }

    @Before
    fun initializeLocalization() {
        Localization.setLanguageCode("en")
    }

    /**
     * Creates the production retained-state owner with deterministic UI publication.
     *
     * @author Dmitriy Moroz
     */
    private fun viewModel() = BackgroundWorkViewModel(mockk(relaxed = true), dispatchers, observer)

    @Test
    fun `given active background work, measured status arrives, expected stage and percentage in the same visible state`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()
            // When
            runCurrent()
            // Then
            assertTrue(viewModel.state.value.running)
            assertEquals("Denoising", viewModel.state.value.subTitle)
            assertEquals(25, viewModel.state.value.percentage)
        }

    @Test
    fun `given measured background work, queue status has no measurement, expected stale percentage removed`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()
            runCurrent()
            // When
            status.value = BackgroundWorkStatus(true, "Running", "Queued")
            runCurrent()
            // Then
            assertTrue(viewModel.state.value.running)
            assertEquals("Queued", viewModel.state.value.subTitle)
            assertNull(viewModel.state.value.percentage)
        }

    @Test
    fun `given measured background work, provider has blank stage text, expected blank status preserved for left-aligned percentage`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()
            runCurrent()
            // When
            status.value = BackgroundWorkStatus(true, "Running", "", 40)
            runCurrent()
            // Then
            assertEquals("", viewModel.state.value.subTitle)
            assertEquals(40, viewModel.state.value.percentage)
        }

    @Test
    fun `given measured background work, successful completion occurs, expected terminal state without stale progress`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()
            runCurrent()
            // When
            result.value = BackgroundWorkResult.Success(emptyList())
            status.value = BackgroundWorkStatus(false, "", "")
            runCurrent()
            // Then
            assertFalse(viewModel.state.value.running)
            assertEquals(true, viewModel.state.value.visible)
            assertEquals(true, viewModel.state.value.dismissible)
            assertNull(viewModel.state.value.percentage)
        }

    @Test
    fun `given measured background work, failure occurs, expected terminal state without stale progress`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()
            runCurrent()
            // When
            result.value = BackgroundWorkResult.Error(IllegalStateException("failed"))
            status.value = BackgroundWorkStatus(false, "", "")
            runCurrent()
            // Then
            assertFalse(viewModel.state.value.running)
            assertEquals(true, viewModel.state.value.visible)
            assertEquals(true, viewModel.state.value.dismissible)
            assertNull(viewModel.state.value.percentage)
        }

    @Test
    fun `given measured background work, cancellation occurs, expected terminal state without stale progress`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()
            runCurrent()
            // When
            result.value = BackgroundWorkResult.None
            status.value = BackgroundWorkStatus(false, "", "")
            runCurrent()
            // Then
            assertFalse(viewModel.state.value.running)
            assertEquals(false, viewModel.state.value.visible)
            assertEquals(false, viewModel.state.value.dismissible)
            assertNull(viewModel.state.value.percentage)
        }

    @Test
    fun `given measured background work, result is dismissed, expected measurement and visibility cleared`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()
            runCurrent()
            // When
            viewModel.processIntent(BackgroundWorkIntent.Dismiss)
            // Then
            assertNull(viewModel.state.value.percentage)
            assertFalse(viewModel.state.value.visible)
            verify(exactly = 1) { observer.dismissResult() }
        }
}
