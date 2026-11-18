package com.shifthackz.aisdv1.presentation.onboarding.navigation

import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCase
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter

/**
 * Maps the completed onboarding decision to setup or configuration loading.
 *
 * @param action Domain launch decision produced after the relevant configuration checks.
 *
 * @author Dmitriy Moroz
 */
fun OnBoardingRouter.postOnBoardingNavigation(
    action: SplashNavigationUseCase.Action,
) = when (action) {
    SplashNavigationUseCase.Action.LAUNCH_ONBOARDING,
    SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP -> navigateToServerSetupAfterOnBoarding()

    SplashNavigationUseCase.Action.LAUNCH_HOME -> navigateToPostOnBoardingConfigLoader()
}
