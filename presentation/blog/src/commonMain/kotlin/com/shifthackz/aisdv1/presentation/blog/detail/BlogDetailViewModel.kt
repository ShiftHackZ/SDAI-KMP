package com.shifthackz.aisdv1.presentation.blog.detail

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.domain.usecase.blog.FetchOrGetBlogPostUseCase
import com.shifthackz.aisdv1.presentation.blog.detail.share.BlogPlatformActions

/**
 * Owns article loading, retries, and sharing for one detail route.
 *
 * @param diagnostics Observer for screen state transitions.
 * @param dispatchersProvider Main dispatcher for publishing state and effects.
 * @param fetchOrGetBlogPost Resolves the selected article from cache or the current feed.
 * @param platformActions Presents the system share chooser.
 * @param slug Identifier of the article selected in the list.
 * @author Dmitriy Moroz
 */
internal class BlogDetailViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val fetchOrGetBlogPost: FetchOrGetBlogPostUseCase,
    private val platformActions: BlogPlatformActions,
    private val slug: String,
) : BaseMviViewModel<BlogDetailState, BlogDetailIntent, BlogDetailEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.BLOG_DETAIL.asDiagnosticScreen(),
    initialState = BlogDetailState.Loading,
    effectDispatcher = dispatchersProvider.immediate,
) {
    init {
        load(shouldRefresh = false)
    }

    override fun processIntent(intent: BlogDetailIntent) {
        when (intent) {
            BlogDetailIntent.Retry -> if (currentState != BlogDetailState.Loading) load(
                shouldRefresh = true
            )

            BlogDetailIntent.Share -> share()
        }
    }

    private fun load(shouldRefresh: Boolean) {
        emitState(BlogDetailState.Loading)
        launch(dispatchersProvider.immediate) {
            val state = fetchOrGetBlogPost(slug, shouldRefresh).fold(
                onSuccess = { post ->
                    post?.let { BlogDetailState.Ready(it) } ?: BlogDetailState.Missing
                },
                onFailure = { BlogDetailState.Error },
            )
            emitState(state)
        }
    }

    private fun share() {
        val post = (currentState as? BlogDetailState.Ready)?.post ?: return
        if (!platformActions.sharePost("${post.title}\n${post.url}")) {
            emitEffect(BlogDetailEffect.ShareFailed)
        }
    }
}
