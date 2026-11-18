package com.shifthackz.aisdv1.presentation.logger

/**
 * Native clipboard and sharing operations for the log viewer.
 *
 * @author Dmitriy Moroz
 */
interface LoggerPlatformActions {
    /**
     * Copies the displayed log snapshot to the native clipboard.
     *
     * @param text Current log content selected by the caller.
     *
     * @author Dmitriy Moroz
     */
    suspend fun copyLogs(text: String)

    /**
     * Presents native sharing for the displayed log snapshot.
     *
     * @param text Current log content selected by the caller.
     *
     * @author Dmitriy Moroz
     */
    suspend fun shareLogs(text: String)
}
