package com.shifthackz.aisdv1.domain.repository

/**
 * Reads request-correlated AUTOMATIC1111 job progress without retrieving preview images.
 *
 * @author Dmitriy Moroz
 */
interface Automatic1111ProgressRepository {
    /**
     * Reads the selected connection's progress, rejecting measurements belonging to other clients.
     * Transport and decoding failures propagate to the caller.
     *
     * @param taskId Exact identity included in the generation request.
     * @return Completion fraction, or null when idle, queued or correlation is unsupported.
     * @author Dmitriy Moroz
     */
    suspend fun getProgress(taskId: String): Float?
}
