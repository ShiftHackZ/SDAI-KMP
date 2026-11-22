package com.shifthackz.aisdv1.presentation.civitai.history

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * History navigation and viewport-driven page requests.
 *
 * @author Dmitriy Moroz
 */
sealed interface CivitaiHistoryIntent : MviIntent {
    /**
     * Requests metadata around the first visible receipt.
     *
     * @param first Global index reported by the lazy list.
     * @author Dmitriy Moroz
     */
    data class Viewport(val first: Int) : CivitaiHistoryIntent

    /**
     * Restarts strict storage observation after a read failure.
     *
     * @author Dmitriy Moroz
     */
    data object Retry : CivitaiHistoryIntent

    /**
     * Returns to Civitai settings without requiring an authorized account.
     *
     * @author Dmitriy Moroz
     */
    data object Back : CivitaiHistoryIntent
}
