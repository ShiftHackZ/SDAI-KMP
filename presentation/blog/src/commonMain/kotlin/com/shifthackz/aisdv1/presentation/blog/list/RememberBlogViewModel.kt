package com.shifthackz.aisdv1.presentation.blog.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import org.koin.mp.KoinPlatform.getKoin

/**
 * Retains the feed ViewModel for the list route and clears it when that route is disposed.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun rememberBlogViewModel(): BlogViewModel {
    val viewModel = remember { getKoin().get<BlogViewModel>() }
    DisposableEffect(viewModel) {
        val store = ViewModelStore()
        store.put("Blog", viewModel)
        onDispose { store.clear() }
    }
    return viewModel
}
