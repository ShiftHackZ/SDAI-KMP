package com.shifthackz.aisdv1.presentation.blog.detail

import com.shifthackz.aisdv1.core.mvi.MviEffect

/**
 * Reports one-shot results from reader actions.
 *
 * @author Dmitriy Moroz
 */
internal sealed interface BlogDetailEffect : MviEffect {
    /**
     * Asks the screen to show feedback when the system chooser cannot open.
     *
     * @author Dmitriy Moroz
     */
    data object ShareFailed : BlogDetailEffect
}
