package com.shifthackz.aisdv1.presentation.logger

/**
 * Provides empty log content on platforms without an application log file.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpLogReader : LogReader {
    override suspend fun read() = ""
}
