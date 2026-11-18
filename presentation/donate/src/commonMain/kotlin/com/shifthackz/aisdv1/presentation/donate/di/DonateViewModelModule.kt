package com.shifthackz.aisdv1.presentation.donate.di

import com.shifthackz.aisdv1.core.navigation.router.DonateRouter
import com.shifthackz.aisdv1.presentation.donate.DonateViewModel
import org.koin.dsl.module


/**
 * Creates donation-page state holders using shared supporter/link operations and a caller-owned router.
 *
 * @author Dmitriy Moroz
 */
val donateViewModelModule = module {
    factory { (router: DonateRouter) ->
        DonateViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            fetchSupportersUseCase = get(),
            linksProvider = get(),
            router = router,
        )
    }
}
