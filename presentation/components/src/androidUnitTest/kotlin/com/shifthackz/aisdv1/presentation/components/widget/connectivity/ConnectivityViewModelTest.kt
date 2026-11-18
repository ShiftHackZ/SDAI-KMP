package com.shifthackz.aisdv1.presentation.components.widget.connectivity

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.usecase.connectivity.GetMonitorConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveMonitorConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveSeverConnectivityUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ConnectivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchersProvider = object : DispatchersProvider {
        override val io: CoroutineDispatcher = testDispatcher
        override val ui: CoroutineDispatcher = testDispatcher
        override val immediate: CoroutineDispatcher = testDispatcher
    }
    private val stubGetMonitorConnectivityUseCase = mockk<GetMonitorConnectivityUseCase>()
    private val stubObserveMonitorConnectivityUseCase = mockk<ObserveMonitorConnectivityUseCase>()
    private val stubObserveSeverConnectivityUseCase = mockk<ObserveSeverConnectivityUseCase>()

    @Before
    fun initialize() {
        every {
            stubGetMonitorConnectivityUseCase()
        } returns true

        every {
            stubObserveMonitorConnectivityUseCase()
        } returns emptyFlow()

        every {
            stubObserveSeverConnectivityUseCase()
        } returns emptyFlow()
    }

    @Test
    fun `given connectivity, initialized with monitoring enabled, expected uninitialized state has enabled true`() =
        runTest(testDispatcher) {
            // Given
            // initialize() stubs monitorConnectivity=true and empty connectivity observation flows before
            // construction.

            // When
            val viewModel = createViewModel()

            // Then
            Assert.assertEquals(
                ConnectivityState.Uninitialized(true),
                viewModel.state.value,
            )

        }

    @Test
    fun `given connectivity, initialized with monitoring disabled, expected uninitialized state has enabled false`() =
        runTest(testDispatcher) {
            // Given

            every {
                stubGetMonitorConnectivityUseCase()
            } returns false

            // When
            val viewModel = createViewModel()

            // Then
            Assert.assertEquals(
                ConnectivityState.Uninitialized(false),
                viewModel.state.value,
            )

        }

    @Test
    fun `given connectivity, enabled monitoring emits a connected server, expected connected state has enabled true`() =
        runTest(testDispatcher) {
            // Given

            every {
                stubObserveMonitorConnectivityUseCase()
            } returns flowOf(true)
            every {
                stubObserveSeverConnectivityUseCase()
            } returns flowOf(true)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                ConnectivityState.Connected(true),
                viewModel.state.value,
            )

        }

    @Test
    fun `given connectivity, disabled monitoring emits a disconnected server, expected disconnected state has enabled false`() =
        runTest(testDispatcher) {
            // Given

            every {
                stubObserveMonitorConnectivityUseCase()
            } returns flowOf(false)
            every {
                stubObserveSeverConnectivityUseCase()
            } returns flowOf(false)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                ConnectivityState.Disconnected(false),
                viewModel.state.value,
            )

        }

    private fun TestScope.createViewModel() = ConnectivityViewModel(
        diagnostics = mockk(relaxed = true),
        observeServerConnectivityUseCase = stubObserveSeverConnectivityUseCase,
        getMonitorConnectivityUseCase = stubGetMonitorConnectivityUseCase,
        observeMonitorConnectivityUseCase = stubObserveMonitorConnectivityUseCase,
        dispatchersProvider = dispatchersProvider,
    )
}
