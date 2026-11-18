package com.shifthackz.aisdv1.presentation.history

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Navigation and reload requests from the generation-history screen.
 *
 * @author Dmitriy Moroz
 */
sealed interface HistoryIntent : MviIntent {
    /**
     * Returns from history to the preceding route.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBack : HistoryIntent

    /**
     * Reloads generation metadata from persistent gallery storage.
     *
     * @author Dmitriy Moroz
     */
    data object Refresh : HistoryIntent
}
