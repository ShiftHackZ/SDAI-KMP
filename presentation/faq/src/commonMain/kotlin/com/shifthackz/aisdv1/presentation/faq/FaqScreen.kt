package com.shifthackz.aisdv1.presentation.faq

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.presentation.faq.content.FaqScreenContent
import org.koin.mp.KoinPlatform.getKoin

/**
 * Binds FAQ loading and retry to the route-scoped ViewModel and retains the active search and filter.
 *
 * @param onBack Returns to the route that opened the FAQ.
 * @param modifier Layout modifiers for the full-screen destination.
 * @author Dmitriy Moroz
 */
@Composable
fun FaqScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = remember { getKoin().get<FaqViewModel>() }
    DisposableEffect(viewModel) {
        val store = ViewModelStore()
        store.put("Faq", viewModel)
        onDispose { store.clear() }
    }
    var query by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf<String?>(null) }

    MviComponent(viewModel = viewModel) { state, processIntent ->
        FaqScreenContent(
            state = state,
            query = query,
            categoryId = categoryId,
            onQueryChange = { query = it },
            onCategoryChange = { categoryId = it },
            onRetry = { processIntent(FaqIntent.Retry) },
            onBack = onBack,
            modifier = modifier,
        )
    }
}
