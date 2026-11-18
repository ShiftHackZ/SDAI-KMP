package com.shifthackz.aisdv1.presentation.donate

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.domain.usecase.donate.FetchSupportersUseCase
import com.shifthackz.aisdv1.core.navigation.router.DonateRouter
import kotlinx.coroutines.withContext

/**
 * Loads supporter acknowledgements and emits an external support link when requested.
 *
 * @param diagnostics Optional bounded MVI transition observer.
 * @param dispatchersProvider Runs supporter fetching on IO and publishes state on the immediate UI dispatcher.
 * @param fetchSupportersUseCase Fetches the supporter list shown on the acknowledgement page.
 * @param linksProvider Supplies the configured external donation destination.
 * @param router Handles back navigation from the donation screen.
 * @param onError Receives supporter-load failures after loading stops.
 *
 * @author Dmitriy Moroz
 */
class DonateViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val fetchSupportersUseCase: FetchSupportersUseCase,
    private val linksProvider: LinksProvider,
    private val router: DonateRouter,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<DonateState, DonateIntent, DonateEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.DONATE.asDiagnosticScreen(),
    initialState = DonateState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        launch(dispatchersProvider.io) {
            runCatching { fetchSupportersUseCase() }
                .onSuccess { supporters ->
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                loading = false,
                                supporters = supporters,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateState { it.copy(loading = false) }
                    }
                    onError(t)
                }
        }
    }

    override fun processIntent(intent: DonateIntent) {
        when (intent) {
            DonateIntent.LaunchDonate -> emitEffect(DonateEffect.OpenUrl(linksProvider.donateUrl))
            DonateIntent.NavigateBack -> router.navigateBack()
        }
    }
}
