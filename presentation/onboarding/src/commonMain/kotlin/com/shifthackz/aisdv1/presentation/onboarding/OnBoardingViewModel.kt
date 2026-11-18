package com.shifthackz.aisdv1.presentation.onboarding

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCase
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter
import com.shifthackz.aisdv1.presentation.onboarding.navigation.postOnBoardingNavigation
import kotlinx.coroutines.withContext

/**
 * Initializes onboarding previews from saved appearance and advances to the correct post-onboarding route.
 * Completion is persisted before navigation; a settings launch returns to the caller, while first launch
 * rechecks configuration off the UI dispatcher.
 *
 * @param diagnostics Optional bounded observer for MVI state transitions.
 * @param launchSource Distinguishes first-launch completion from revisiting onboarding through settings.
 * @param dispatchersProvider Runs configuration checks on IO and publishes navigation on the UI dispatcher.
 * @param router Receives setup, loader or back navigation after completion.
 * @param splashNavigationUseCase Reassesses saved configuration after first-launch onboarding.
 * @param preferenceManager Supplies preview appearance and stores the completed-onboarding flag.
 * @param buildInfoProvider Supplies the version displayed in the appearance preview.
 *
 * @author Dmitriy Moroz
 */
class OnBoardingViewModel(
    diagnostics: UiDiagnostics,
    val launchSource: LaunchSource,
    private val dispatchersProvider: DispatchersProvider,
    private val router: OnBoardingRouter,
    private val splashNavigationUseCase: SplashNavigationUseCase,
    private val preferenceManager: PreferenceManager,
    private val buildInfoProvider: BuildInfoProvider,
) : BaseMviViewModel<OnBoardingState, OnBoardingIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.ON_BOARDING.asDiagnosticScreen(),
    initialState = OnBoardingState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        emitState(
            OnBoardingState(
                darkThemeToken = DarkThemeToken.parse(preferenceManager.designDarkThemeToken),
                appVersion = buildInfoProvider.toString(),
                showLocalDiffusionPage = isLocalDiffusionOnBoardingAvailable(),
            )
        )
    }

    override fun processIntent(intent: OnBoardingIntent) {
        when (intent) {
            OnBoardingIntent.Navigate -> navigate()
        }
    }

    private fun navigate() {
        preferenceManager.onBoardingComplete = true
        when (launchSource) {
            LaunchSource.SPLASH -> launch(dispatchersProvider.io) {
                runCatching { splashNavigationUseCase() }
                    .onSuccess { action ->
                        withContext(dispatchersProvider.immediate) {
                            router.postOnBoardingNavigation(action)
                        }
                    }
                    .onFailure { }
            }

            LaunchSource.SETTINGS -> router.navigateBack()
        }
    }
}
