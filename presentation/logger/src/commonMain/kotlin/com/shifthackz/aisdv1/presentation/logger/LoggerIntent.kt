package com.shifthackz.aisdv1.presentation.logger

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Log reload, native copy/share and route-navigation requests.
 *
 * @author Dmitriy Moroz
 */
sealed interface LoggerIntent : MviIntent {
    /**
     * Reloads the current application log file.
     *
     * @author Dmitriy Moroz
     */
    data object ReadLogs : LoggerIntent

    /**
     * Copies the displayed nonblank log snapshot.
     *
     * @author Dmitriy Moroz
     */
    data object CopyLogs : LoggerIntent

    /**
     * Shares the displayed nonblank log snapshot through the native adapter.
     *
     * @author Dmitriy Moroz
     */
    data object ShareLogs : LoggerIntent

    /**
     * Returns to the preceding application route.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBack : LoggerIntent
}
