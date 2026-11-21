package com.shifthackz.aisdv1.presentation.app.di

import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.presentation.app.AiSdAppViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.dsl.module

/**
 * Creates one shell observer per host router and retained generation activity stream.
 *
 * @author Dmitriy Moroz
 */
internal val appViewModelModule = module {
    factory { (router: RootAppRouter, generationActivity: Flow<Boolean>) ->
        AiSdAppViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            preferenceManager = get(),
            router = router,
            generationActivity = generationActivity,
        )
    }
}
