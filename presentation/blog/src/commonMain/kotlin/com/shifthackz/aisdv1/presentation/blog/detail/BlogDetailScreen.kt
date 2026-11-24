package com.shifthackz.aisdv1.presentation.blog.detail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.MviComponent
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Reads a published post in the app and shares its website link through the system chooser.
 *
 * @param slug Article selected in the feed.
 * @param onBack Returns to the blog list.
 * @param modifier Placement of the full-screen reader.
 * @author Dmitriy Moroz
 */
@Composable
fun BlogDetailScreen(
    slug: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember(slug) { getKoin().get<BlogDetailViewModel> { parametersOf(slug) } }
    DisposableEffect(viewModel) {
        val store = ViewModelStore()
        store.put("BlogDetail", viewModel)
        onDispose { store.clear() }
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val shareError by rememberUpdatedState(Localization.string("blog_share_error"))

    MviComponent(
        viewModel = viewModel,
        processEffect = { effect ->
            when (effect) {
                BlogDetailEffect.ShareFailed -> scope.launch {
                    snackbarHostState.showSnackbar(
                        shareError
                    )
                }
            }
        },
    ) { state, processIntent ->
        BlogDetailScreenContent(
            state = state,
            onRetry = { processIntent(BlogDetailIntent.Retry) },
            onBack = onBack,
            onShare = { processIntent(BlogDetailIntent.Share) },
            snackbarHostState = snackbarHostState,
            modifier = modifier,
        )
    }
}
