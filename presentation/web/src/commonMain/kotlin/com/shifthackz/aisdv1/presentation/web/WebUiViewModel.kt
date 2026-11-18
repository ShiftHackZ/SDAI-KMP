package com.shifthackz.aisdv1.presentation.web

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.core.navigation.router.WebUiRouter

/**
 * Captures the selected provider and its saved WebUI URL when the route is created.
 *
 * @param diagnostics Optional bounded MVI transition observer.
 * @param dispatchersProvider Supplies the immediate dispatcher for screen effects.
 * @param preferenceManager Supplies the current provider and saved AUTOMATIC1111/SwarmUI endpoint.
 * @param router Returns from the embedded page to the preceding application route.
 *
 * @author Dmitriy Moroz
 */
class WebUiViewModel(
    diagnostics: UiDiagnostics,
    dispatchersProvider: DispatchersProvider,
    preferenceManager: PreferenceManager,

    private val router: WebUiRouter,
) : BaseMviViewModel<WebUiState, WebUiIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.WEB_UI.asDiagnosticScreen(),
    initialState = WebUiState(
        loading = false,
        source = preferenceManager.source,
        url = preferenceManager.webUiUrl(),
    ),
    effectDispatcher = dispatchersProvider.immediate,
) {

    override fun processIntent(intent: WebUiIntent) {
        when (intent) {
            WebUiIntent.NavigateBack -> router.navigateBack()
        }
    }
}


private fun PreferenceManager.webUiUrl(): String = when (source) {
    ServerSource.AUTOMATIC1111 -> automatic1111ServerUrl
    ServerSource.SWARM_UI -> swarmUiServerUrl
    else -> ""
}
