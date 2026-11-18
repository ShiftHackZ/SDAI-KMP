package com.shifthackz.aisdv1.presentation.debug.di

import com.shifthackz.aisdv1.core.navigation.router.DebugMenuRouter
import com.shifthackz.aisdv1.presentation.debug.DebugMenuPlatformActions
import com.shifthackz.aisdv1.presentation.debug.DebugMenuViewModel
import org.koin.dsl.module

/**
 * Creates developer-menu ViewModels with the route owner and platform actions supplied by the screen.
 *
 * @author Dmitriy Moroz
 */
val debugViewModelModule = module {
    factory { (router: DebugMenuRouter, platformActions: DebugMenuPlatformActions) ->
        DebugMenuViewModel(
            diagnosticReports = get(),
            diagnostics = get(),
            dispatchersProvider = get(),
            preferenceManager = get(),
            debugInsertBadBase64UseCase = get(),
            router = router,
            platformActions = platformActions,
        )
    }
}
