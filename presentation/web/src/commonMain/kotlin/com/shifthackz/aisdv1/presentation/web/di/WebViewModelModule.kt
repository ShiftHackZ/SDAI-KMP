package com.shifthackz.aisdv1.presentation.web.di

import com.shifthackz.aisdv1.core.navigation.router.WebUiRouter
import com.shifthackz.aisdv1.presentation.web.WebUiViewModel
import org.koin.dsl.module


/**
 * Creates provider-WebUI state holders with the route owner supplied by application composition.
 *
 * @author Dmitriy Moroz
 */
val webViewModelModule = module {
    factory { (router: WebUiRouter) ->
        WebUiViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            preferenceManager = get(),
            router = router,
        )
    }
}
