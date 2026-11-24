package com.shifthackz.aisdv1.presentation.blog.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.blog.content.BlogCachedNotice
import com.shifthackz.aisdv1.presentation.blog.content.BlogLoadError
import com.shifthackz.aisdv1.presentation.blog.content.BlogLoading
import com.shifthackz.aisdv1.presentation.blog.content.BlogScaffold
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver

/**
 * Displays the current article list, its loading state, or recovery from a feed error.
 *
 * @param state Feed resolved by the route.
 * @param onRetry Requests a fresh download.
 * @param onBack Returns to Settings.
 * @param onOpenPost Opens the selected article slug.
 * @param modifier Placement of the list screen.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BlogScreenContent(
    state: BlogState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    BlogScaffold(onBack = onBack, modifier = modifier) {
        val toolbarInset = LocalToolbarContentInset.current
        val body = Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp)
        val status = body.fillMaxSize().padding(top = toolbarInset)
        ToolbarScrollObserver(state is BlogState.Ready && listState.canScrollBackward)
        when (state) {
            BlogState.Loading -> BlogLoading(modifier = status)
            BlogState.Error -> BlogLoadError(
                messageKey = "blog_load_error",
                onRetry = onRetry,
                modifier = status
            )

            is BlogState.Ready -> LazyColumn(
                state = listState,
                modifier = body.fillMaxSize().verticalScrollbar(listState, topInset = toolbarInset),
                contentPadding = PaddingValues(
                    start = 16.dp, top = 16.dp + toolbarInset, end = 16.dp, bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.content.isCached) {
                    item(key = "cached") { BlogCachedNotice(onRetry = onRetry) }
                }
                items(state.content.posts) { post ->
                    BlogPostCard(post = post, onOpen = { onOpenPost(post.slug) })
                }
            }
        }
    }
}
