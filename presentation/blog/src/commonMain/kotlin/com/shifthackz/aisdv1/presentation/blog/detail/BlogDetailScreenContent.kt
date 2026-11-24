package com.shifthackz.aisdv1.presentation.blog.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.blog.content.BlogLoadError
import com.shifthackz.aisdv1.presentation.blog.content.BlogLoading
import com.shifthackz.aisdv1.presentation.blog.content.BlogScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver

/**
 * Shows the selected article with a toolbar action for sharing its canonical link.
 *
 * @param state Load result for the selected article.
 * @param onRetry Requests fresh article data.
 * @param onBack Returns to the list.
 * @param onShare Requests sharing the currently displayed article.
 * @param snackbarHostState Presents a message when the chooser cannot be opened.
 * @param modifier Placement of the reading screen.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogDetailScreenContent(
    state: BlogDetailState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val isArticleReady = state is BlogDetailState.Ready
    BlogScaffold(
        onBack = onBack,
        modifier = modifier,
        actions = {
            if (isArticleReady) {
                ToolbarIconButton(onClick = onShare) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = Localization.string("blog_share_post")
                    )
                }
            }
        },
    ) {
        val toolbarInset = LocalToolbarContentInset.current
        val body = Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp).fillMaxSize()
        val status = body.padding(top = toolbarInset)
        if (!isArticleReady) ToolbarScrollObserver(false)
        when (state) {
            BlogDetailState.Loading -> BlogLoading(modifier = status)
            BlogDetailState.Error, BlogDetailState.Missing -> BlogLoadError(
                messageKey = if (state == BlogDetailState.Error) "blog_load_error" else "blog_post_missing",
                onRetry = onRetry,
                modifier = status,
            )

            is BlogDetailState.Ready -> BlogArticle(post = state.post, modifier = body)
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
