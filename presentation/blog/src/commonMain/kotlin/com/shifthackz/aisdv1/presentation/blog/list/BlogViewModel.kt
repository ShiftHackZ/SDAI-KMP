package com.shifthackz.aisdv1.presentation.blog.list

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.domain.usecase.blog.FetchOrGetBlogUseCase

/**
 * Loads the published feed on list entry and serializes requests to refresh it.
 *
 * @param diagnostics Observer for screen state transitions.
 * @param dispatchersProvider Main dispatcher used to publish state.
 * @param fetchOrGetBlog Blog loader with offline recovery.
 * @author Dmitriy Moroz
 */
internal class BlogViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val fetchOrGetBlog: FetchOrGetBlogUseCase,
) : BaseMviViewModel<BlogState, BlogIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.BLOG.asDiagnosticScreen(),
    initialState = BlogState.Loading,
    effectDispatcher = dispatchersProvider.immediate,
) {
    init {
        load()
    }

    override fun processIntent(intent: BlogIntent) {
        when (intent) {
            BlogIntent.Retry -> if (currentState != BlogState.Loading) load()
        }
    }

    private fun load() {
        emitState(BlogState.Loading)
        launch(dispatchersProvider.immediate) {
            val state = fetchOrGetBlog().fold(
                onSuccess = { BlogState.Ready(it) },
                onFailure = { BlogState.Error },
            )
            emitState(state)
        }
    }
}
