package com.shifthackz.aisdv1.presentation.logger.di

import com.shifthackz.aisdv1.core.navigation.router.LoggerRouter
import com.shifthackz.aisdv1.presentation.logger.LoggerViewModel
import org.koin.dsl.module

/**
 * Creates log-viewer state holders with shared log/native-action services and a caller-owned router.
 *
 * @author Dmitriy Moroz
 */
val loggerViewModelModule = module {
    factory { (router: LoggerRouter) ->
        LoggerViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            logReader = get(),
            platformActions = get(),
            router = router,
        )
    }
}
