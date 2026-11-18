package com.shifthackz.aisdv1.presentation.splash.di

import com.shifthackz.aisdv1.core.navigation.router.SplashRouter
import com.shifthackz.aisdv1.presentation.splash.SplashViewModel
import org.koin.dsl.module

/**
 * Creates startup route-selection ViewModels with the supplied root splash-navigation owner.
 *
 * @author Dmitriy Moroz
 */
val splashViewModelModule = module {
    factory { (splashRouter: SplashRouter) ->
        SplashViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            splashNavigationUseCase = get(),
            splashRouter = splashRouter,
        )
    }
}
