package com.shifthackz.aisdv1.presentation.logger

/**
 * Reads the platform log cache for display; unavailable platforms can supply an empty reader.
 *
 * @author Dmitriy Moroz
 */
interface LogReader {
    /**
     * Returns current log text; filesystem and decoding failures propagate to the caller.
     *
     * @author Dmitriy Moroz
     */
    suspend fun read(): String
}
