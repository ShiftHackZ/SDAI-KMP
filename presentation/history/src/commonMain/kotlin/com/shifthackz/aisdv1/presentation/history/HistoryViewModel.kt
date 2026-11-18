package com.shifthackz.aisdv1.presentation.history

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.core.navigation.router.HistoryRouter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Loads generation-history metadata at creation and on explicit refresh, retaining user-visible read errors.
 *
 * @param diagnostics Optional bounded MVI transition observer.
 * @param dispatchersProvider Runs persistent reads on IO and publishes rendered state on the UI dispatcher.
 * @param galleryReads Reads generation history without loading original image payloads into list state.
 * @param router Handles back navigation from the history screen.
 * @param onError Reports history-load failures after the error state is published.
 *
 * @author Dmitriy Moroz
 */
class HistoryViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val galleryReads: GalleryReadGateway,
    private val router: HistoryRouter,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<HistoryState, HistoryIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.HISTORY.asDiagnosticScreen(),
    initialState = HistoryState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        loadHistory()
    }

    override fun processIntent(intent: HistoryIntent) {
        when (intent) {
            HistoryIntent.NavigateBack -> router.navigateBack()
            HistoryIntent.Refresh -> loadHistory()
        }
    }

    private fun loadHistory() {
        updateState { it.copy(loading = true, error = null) }
        launch(dispatchersProvider.io) {
            runCatching { galleryReads.history() }
                .onSuccess { items ->
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                loading = false,
                                items = items,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                loading = false,
                                error = t.message ?: "Unable to load history",
                            )
                        }
                    }
                    onError(t)
                }
        }
    }
}
