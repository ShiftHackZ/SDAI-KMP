package com.shifthackz.aisdv1.presentation.components.widget.work

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * User actions for the shared background-generation status card.
 *
 * @author Dmitriy Moroz
 */
sealed interface BackgroundWorkIntent : MviIntent {
    /**
     * Dismisses the completed or failed work notice without cancelling active generation.
     *
     * @author Dmitriy Moroz
     */
    data object Dismiss : BackgroundWorkIntent
}
