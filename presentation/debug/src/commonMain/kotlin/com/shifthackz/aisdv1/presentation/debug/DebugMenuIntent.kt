package com.shifthackz.aisdv1.presentation.debug

import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Developer actions for diagnostics, local-runtime preferences and background-work controls.
 *
 * @author Dmitriy Moroz
 */
sealed interface DebugMenuIntent : MviIntent {
    /**
     * Requests sharing of the current bounded diagnostic snapshot.
     *
     * @author Dmitriy Moroz
     */
    data object ShareUiCapture : DebugMenuIntent

    /**
     * Clears the bounded diagnostic event buffer and counters.
     *
     * @author Dmitriy Moroz
     */
    data object ResetUiCapture : DebugMenuIntent

    /**
     * Stops collecting further diagnostic events.
     *
     * @author Dmitriy Moroz
     */
    data object StopUiCapture : DebugMenuIntent

    /**
     * Starts explicitly requested diagnostic capture.
     *
     * @author Dmitriy Moroz
     */
    data object StartUiCapture : DebugMenuIntent

    /**
     * Returns to the previous route.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBack : DebugMenuIntent

    /**
     * Opens the application log viewer.
     *
     * @author Dmitriy Moroz
     */
    data object ViewLogs : DebugMenuIntent

    /**
     * Deletes application-owned cached log files.
     *
     * @author Dmitriy Moroz
     */
    data object ClearLogs : DebugMenuIntent

    /**
     * Toggles the local ONNX cancellation preference.
     *
     * @author Dmitriy Moroz
     */
    data object AllowLocalDiffusionCancel : DebugMenuIntent

    /**
     * Inserts a deliberately malformed image for gallery decoder checks.
     *
     * @author Dmitriy Moroz
     */
    data object InsertBadBase64 : DebugMenuIntent

    /**
     * Opens or confirms the local-inference scheduler preference.
     *
     * @author Dmitriy Moroz
     */
    sealed interface LocalDiffusionScheduler : DebugMenuIntent {

        /**
         * Stores the selected local ONNX scheduler and closes the selection sheet.
         *
         * @param token Scheduler selected by the user.
         *
         * @author Dmitriy Moroz
         */
        data class Confirm(val token: SchedulersToken) : DebugMenuIntent

        /**
         * Opens the scheduler sheet with the current selection.
         *
         * @author Dmitriy Moroz
         */
        data object Request : DebugMenuIntent
    }

    /**
     * Background-work commands to cancel all jobs or resubmit the last captured request of either type.
     *
     * @author Dmitriy Moroz
     */
    enum class WorkManager : DebugMenuIntent {
        CancelAll, RestartTxt2Img, RestartImg2Img;
    }

    /**
     * Closes the active debug selection sheet.
     *
     * @author Dmitriy Moroz
     */
    data object DismissModal : DebugMenuIntent
}
