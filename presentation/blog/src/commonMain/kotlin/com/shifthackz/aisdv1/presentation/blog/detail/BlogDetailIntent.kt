package com.shifthackz.aisdv1.presentation.blog.detail

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * User actions handled by the article reader.
 *
 * @author Dmitriy Moroz
 */
internal sealed interface BlogDetailIntent : MviIntent {
    /**
     * Tries the current website feed again after an error or a missing article.
     *
     * @author Dmitriy Moroz
     */
    data object Retry : BlogDetailIntent

    /**
     * Opens the system share chooser for the article currently being read.
     *
     * @author Dmitriy Moroz
     */
    data object Share : BlogDetailIntent
}
