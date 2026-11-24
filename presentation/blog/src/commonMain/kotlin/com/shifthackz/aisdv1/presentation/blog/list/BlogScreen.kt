package com.shifthackz.aisdv1.presentation.blog.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent

/**
 * Opens the native feed and routes a selected article to its reading destination.
 *
 * @param onBack Returns to Settings.
 * @param onOpenPost Opens the article identified by the selected slug.
 * @param modifier Placement of the full-screen list.
 * @author Dmitriy Moroz
 */
@Composable
fun BlogScreen(
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    MviComponent(viewModel = rememberBlogViewModel()) { state, processIntent ->
        BlogScreenContent(
            state = state,
            onRetry = { processIntent(BlogIntent.Retry) },
            onBack = onBack,
            onOpenPost = onOpenPost,
            modifier = modifier,
        )
    }
}
