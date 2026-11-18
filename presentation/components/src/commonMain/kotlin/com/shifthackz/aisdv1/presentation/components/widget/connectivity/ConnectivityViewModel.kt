package com.shifthackz.aisdv1.presentation.components.widget.connectivity

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticComponent
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.EmptyIntent
import com.shifthackz.aisdv1.domain.usecase.connectivity.GetMonitorConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveMonitorConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveSeverConnectivityUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/**
 * Combines provider reachability with indicator visibility into a distinct home-status stream.
 *
 * @param diagnostics Optional bounded observer for connectivity widget transitions.
 * @param dispatchersProvider Observes connectivity on IO and publishes status on the immediate UI dispatcher.
 * @param observeServerConnectivityUseCase Reports reachability of the selected provider.
 * @param getMonitorConnectivityUseCase Supplies initial indicator visibility before its stream emits.
 * @param observeMonitorConnectivityUseCase Reports subsequent connectivity-indicator visibility changes.
 * @param onError Receives failures from connectivity or visibility observation.
 *
 * @author Dmitriy Moroz
 */
class ConnectivityViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val observeServerConnectivityUseCase: ObserveSeverConnectivityUseCase,
    private val getMonitorConnectivityUseCase: GetMonitorConnectivityUseCase,
    private val observeMonitorConnectivityUseCase: ObserveMonitorConnectivityUseCase,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<ConnectivityState, EmptyIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = UiDiagnosticComponent.CONNECTIVITY,
    initialState = ConnectivityState.Uninitialized(
        enabled = getMonitorConnectivityUseCase(),
    ),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        launch(dispatchersProvider.io) {
            combine(
                observeServerConnectivityUseCase(),
                observeMonitorConnectivityUseCase()
                    .onStart { emit(getMonitorConnectivityUseCase()) }
                    .distinctUntilChanged(),
            ) { connected, enabled ->
                ConnectivityState.consume(connected to enabled)
            }
                .distinctUntilChanged()
                .catch { onError(it) }
                .collect { state ->
                    withContext(dispatchersProvider.immediate) {
                        emitState(state)
                    }
                }
        }
    }

    override fun processIntent(intent: EmptyIntent) = Unit
}
