package com.shifthackz.aisdv1.presentation.blog.list

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.BlogContent

/**
 * Loading and content states for the blog list.
 *
 * @author Dmitriy Moroz
 */
internal sealed interface BlogState : MviState {
    /**
     * Displays placeholders while the feed resolves.
     *
     * @author Dmitriy Moroz
     */
    data object Loading : BlogState

    /**
     * Offers a retry when no usable feed was available.
     *
     * @author Dmitriy Moroz
     */
    data object Error : BlogState

    /**
     * Shows available articles and their source.
     *
     * @param content Current online or saved feed.
     * @author Dmitriy Moroz
     */
    data class Ready(val content: BlogContent) : BlogState
}
