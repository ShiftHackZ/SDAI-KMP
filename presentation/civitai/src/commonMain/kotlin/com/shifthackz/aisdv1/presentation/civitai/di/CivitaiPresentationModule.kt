package com.shifthackz.aisdv1.presentation.civitai.di

import com.shifthackz.aisdv1.core.navigation.router.CivitaiRouter
import com.shifthackz.aisdv1.presentation.civitai.settings.CivitaiViewModel
import com.shifthackz.aisdv1.presentation.civitai.history.CivitaiHistoryViewModel
import com.shifthackz.aisdv1.presentation.civitai.upload.CivitaiUploadViewModel
import org.koin.dsl.module

/**
 * Installs screen-owned ViewModels against domain contracts for both native hosts.
 *
 * @author Dmitriy Moroz
 */
val civitaiPresentationModule = module {
    factory { (router: CivitaiRouter) ->
        CivitaiHistoryViewModel(
            get(),
            get(),
            get(),
            router,
            get()
        )
    }
    factory { (imageId: Long) ->
        CivitaiUploadViewModel(
            imageId,
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory { (router: CivitaiRouter) -> CivitaiViewModel(get(), get(), get(), router, get()) }
}
