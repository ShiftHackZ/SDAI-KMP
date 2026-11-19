package com.shifthackz.aisdv1.presentation.splash.navigation

import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCase
import com.shifthackz.aisdv1.core.navigation.router.SplashRouter

/**
 * Applies the domain launch decision through presentation-owned navigation actions.
 *
 * @param action Domain launch decision produced after the relevant configuration checks.
 *
 * @author Dmitriy Moroz
 */
fun SplashRouter.postSplashNavigation(
    action: SplashNavigationUseCase.Action,
) {
    when (action) {
        SplashNavigationUseCase.Action.LAUNCH_BACKUP_WELCOME -> navigateToBackupWelcome()
        SplashNavigationUseCase.Action.LAUNCH_INITIAL_RESTORE -> navigateToInitialRestore()
        SplashNavigationUseCase.Action.LAUNCH_BACKUP_RESTORE -> navigateToBackupRestore()
        SplashNavigationUseCase.Action.LAUNCH_ONBOARDING -> navigateToOnBoardingFromSplash()
        SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP -> navigateToServerSetupFromSplash()
        SplashNavigationUseCase.Action.LAUNCH_HOME -> navigateToPostSplashConfigLoader()
    }
}
