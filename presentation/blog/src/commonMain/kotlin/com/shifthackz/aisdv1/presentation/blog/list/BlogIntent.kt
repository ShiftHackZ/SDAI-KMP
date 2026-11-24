package com.shifthackz.aisdv1.presentation.blog.list

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * User actions that request a new blog load.
 *
 * @author Dmitriy Moroz
 */
internal sealed interface BlogIntent : MviIntent {
    /**
     * Tries to load a fresh feed after an error or while showing an offline snapshot.
     *
     * @author Dmitriy Moroz
     */
    data object Retry : BlogIntent
}
