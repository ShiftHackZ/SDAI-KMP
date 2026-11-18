package com.shifthackz.aisdv1.presentation.generation.modal.history

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Pagination and retry requests from the generation-input history selector.
 *
 * @author Dmitriy Moroz
 */
sealed interface InputHistoryIntent : MviIntent {

    /**
     * Requests the next metadata page after the current page is visible.
     *
     * @author Dmitriy Moroz
     */
    data object LoadNextPage : InputHistoryIntent

    /**
     * Retries a failed history-page read.
     *
     * @author Dmitriy Moroz
     */
    data object Retry : InputHistoryIntent
}
