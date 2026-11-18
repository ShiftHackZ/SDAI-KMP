package com.shifthackz.aisdv1.presentation.history.di

import com.shifthackz.aisdv1.core.navigation.router.HistoryRouter
import com.shifthackz.aisdv1.presentation.history.HistoryViewModel
import org.koin.dsl.module


/**
 * Creates history metadata state holders with the supplied back-navigation owner.
 *
 * @author Dmitriy Moroz
 */
val historyViewModelModule = module {
    factory { (router: HistoryRouter) ->
        HistoryViewModel(
            galleryReads = get(),
            diagnostics = get(),
            dispatchersProvider = get(),
            router = router,
        )
    }
}
