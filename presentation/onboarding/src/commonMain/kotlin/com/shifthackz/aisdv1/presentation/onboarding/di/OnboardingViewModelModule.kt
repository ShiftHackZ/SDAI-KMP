package com.shifthackz.aisdv1.presentation.onboarding.di

import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingViewModel
import org.koin.dsl.module

/**
 * Creates onboarding state holders for the supplied launch source and navigation owner.
 *
 * @author Dmitriy Moroz
 */
val onboardingViewModelModule = module {
    factory { (launchSource: LaunchSource, router: OnBoardingRouter) ->
        OnBoardingViewModel(
            diagnostics = get(),
            launchSource = launchSource,
            dispatchersProvider = get(),
            router = router,
            setBackupStartupStage = get(),
            preferenceManager = get(),
            buildInfoProvider = get(),
        )
    }
}
