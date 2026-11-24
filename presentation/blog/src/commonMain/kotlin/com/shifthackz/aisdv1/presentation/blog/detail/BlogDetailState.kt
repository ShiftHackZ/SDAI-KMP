package com.shifthackz.aisdv1.presentation.blog.detail

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.BlogPost

/**
 * Represents the load result for the article selected by the detail route.
 *
 * @author Dmitriy Moroz
 */
internal sealed interface BlogDetailState : MviState {
    /**
     * Shows reading placeholders while the selected post is resolved.
     *
     * @author Dmitriy Moroz
     */
    data object Loading : BlogDetailState

    /**
     * Offers a retry after a feed failure left the article unreadable.
     *
     * @author Dmitriy Moroz
     */
    data object Error : BlogDetailState

    /**
     * The available feed did not contain the selected article.
     *
     * @author Dmitriy Moroz
     */
    data object Missing : BlogDetailState

    /**
     * Provides the article to render and share.
     *
     * @param post Selected article from the website or saved copy.
     * @author Dmitriy Moroz
     */
    data class Ready(val post: BlogPost) : BlogDetailState
}
