package com.shifthackz.aisdv1.presentation.app

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticComponent
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.core.navigation.asHomeTabRoute
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Projects router and display-policy changes into shell state without owning child ViewModels.
 * Observation lasts until the host releases this ViewModel; retained tabs survive overlay navigation.
 *
 * @param diagnostics Optional bounded observer for root-shell state transitions.
 * @param dispatchersProvider Supplies the immediate UI dispatcher for shell observation.
 * @param preferenceManager Supplies background-generation and screen-dimming preferences.
 * @param router Existing navigation authority, including drawer and Back history.
 * @param generationActivity Emits whether either retained generation form is generating.
 * @author Dmitriy Moroz
 */
internal class AiSdAppViewModel(
    diagnostics: UiDiagnostics,
    dispatchersProvider: DispatchersProvider,
    preferenceManager: PreferenceManager,
    private val router: RootAppRouter,
    generationActivity: Flow<Boolean>,
) : BaseMviViewModel<AiSdAppState, AiSdAppIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = UiDiagnosticComponent.ROOT,
    initialState = AiSdAppState(
        route = router.route.value,
        homeRoute = router.route.value.asHomeTabRoute(),
        renderedHomeRoutes = listOfNotNull(router.route.value.asHomeTabRoute()),
        shouldShowHomeChrome = router.route.value.asHomeTabRoute() != null,
        canNavigateBack = router.drawerOpen.value || router.canNavigateBack.value,
        shouldDimScreen = preferenceManager.shouldDimScreen,
    ),
    effectDispatcher = dispatchersProvider.immediate,
) {
    init {
        launch(dispatchersProvider.immediate) {
            combine(
                router.route,
                router.drawerOpen,
                router.canNavigateBack,
                preferenceManager.observe().map { it.backgroundGeneration to it.shouldDimScreen }
                    .distinctUntilChanged(),
                generationActivity.distinctUntilChanged(),
            ) { route, isDrawerOpen, canNavigateBack, displaySettings, isGenerating ->
                val selectedHomeRoute = route.asHomeTabRoute()
                currentState.copy(
                    route = route,
                    homeRoute = selectedHomeRoute ?: currentState.homeRoute,
                    renderedHomeRoutes = (currentState.renderedHomeRoutes + listOfNotNull(
                        selectedHomeRoute
                    )).distinct(),
                    shouldShowHomeChrome = selectedHomeRoute != null,
                    canNavigateBack = isDrawerOpen || canNavigateBack,
                    isForegroundGenerating = isGenerating && !displaySettings.first,
                    shouldDimScreen = displaySettings.second,
                )
            }.distinctUntilChanged().collect { state -> updateState { state } }
        }
    }

    /**
     * Delegates Back handling to the router so its drawer-first policy and stack stay authoritative.
     *
     * @param intent Shell action requested by the platform Back handler.
     * @author Dmitriy Moroz
     */
    override fun processIntent(intent: AiSdAppIntent) {
        when (intent) {
            AiSdAppIntent.NavigateBack -> router.navigateBack()
        }
    }
}
