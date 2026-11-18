package com.shifthackz.aisdv1.presentation.splash

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.EmptyIntent
import com.shifthackz.aisdv1.core.mvi.EmptyState
import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCase
import com.shifthackz.aisdv1.core.navigation.router.SplashRouter
import com.shifthackz.aisdv1.presentation.splash.navigation.postSplashNavigation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Selects the initial route from saved configuration, falling back to setup on noncancellation failure.
 *
 * @param diagnostics Optional bounded MVI transition observer.
 * @param dispatchersProvider Runs launch checks on IO and sends route changes on the immediate UI dispatcher.
 * @param splashNavigationUseCase Determines onboarding, setup or home loading from persisted configuration.
 * @param splashRouter Receives the selected startup route or setup fallback.
 * @param onError Reports failed launch checks after fallback navigation.
 *
 * @author Dmitriy Moroz
 */
class SplashViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val splashNavigationUseCase: SplashNavigationUseCase,
    private val splashRouter: SplashRouter,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<EmptyState, EmptyIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.SPLASH.asDiagnosticScreen(),
    initialState = EmptyState,
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        launch(dispatchersProvider.io) {
            runCatching { splashNavigationUseCase() }
                .onSuccess { action ->
                    withContext(dispatchersProvider.immediate) {
                        splashRouter.postSplashNavigation(action)
                    }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    withContext(dispatchersProvider.immediate) {
                        splashRouter.navigateToServerSetupFromSplash()
                    }
                    onError(t)
                }
        }
    }

    override fun processIntent(intent: EmptyIntent) = Unit
}
