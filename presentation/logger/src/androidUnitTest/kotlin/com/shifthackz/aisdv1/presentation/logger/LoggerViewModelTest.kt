package com.shifthackz.aisdv1.presentation.logger

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.router.LoggerRouter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoggerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchersProvider = object : DispatchersProvider {
        override val io: CoroutineDispatcher = testDispatcher
        override val ui: CoroutineDispatcher = testDispatcher
        override val immediate: CoroutineDispatcher = testDispatcher
    }
    private val stubLogReader = mockk<LogReader>()
    private val stubPlatformActions = mockk<LoggerPlatformActions>(relaxed = true)
    private val stubRouter = mockk<LoggerRouter>(relaxed = true)

    @Test
    fun `given logger, initial log reading succeeds, expected loading ends with the returned log text`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                stubLogReader.read()
            } returns "Sample log line"

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                LoggerState(
                    loading = false,
                    text = "Sample log line",
                ),
                viewModel.state.value,
            )
        }

    @Test
    fun `given logger, initial log reading fails, expected loading ends with empty log text`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                stubLogReader.read()
            } throws IllegalStateException("No logs")

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                LoggerState(
                    loading = false,
                    text = "",
                ),
                viewModel.state.value,
            )
        }

    @Test
    fun `given logger, received NavigateBack intent, expected router navigateBack called`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                stubLogReader.read()
            } returns ""

            // When
            val viewModel = createViewModel()
            viewModel.processIntent(LoggerIntent.NavigateBack)

            // Then
            verify {
                stubRouter.navigateBack()
            }
        }

    @Test
    fun `given logger, received CopyLogs intent with loaded logs, expected platform copyLogs called`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                stubLogReader.read()
            } returns "Sample log line"

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.processIntent(LoggerIntent.CopyLogs)
            advanceUntilIdle()

            // Then
            coVerify {
                stubPlatformActions.copyLogs("Sample log line")
            }
        }

    @Test
    fun `given logger, received ShareLogs intent with loaded logs, expected platform shareLogs called`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                stubLogReader.read()
            } returns "Sample log line"

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.processIntent(LoggerIntent.ShareLogs)
            advanceUntilIdle()

            // Then
            coVerify {
                stubPlatformActions.shareLogs("Sample log line")
            }
        }

    private fun TestScope.createViewModel() = LoggerViewModel(
        diagnostics = mockk(relaxed = true),
        dispatchersProvider = dispatchersProvider,
        logReader = stubLogReader,
        platformActions = stubPlatformActions,
        router = stubRouter,
    )
}
