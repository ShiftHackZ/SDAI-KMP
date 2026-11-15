package com.shifthackz.aisdv1.work

import java.util.UUID

/**
 * Retains immutable payloads while WorkManager can recreate an unfinished request.
 *
 * @author Dmitriy Moroz
 */
interface GenerationWorkPayloadCleanup {
    /**
     * Cleans a terminal request, preserving an OS-stopped request until persisted state is terminal.
     *
     * @param id Immutable work identity.
     * @param isStopped Whether WorkManager stopped this invocation.
     * @author Dmitriy Moroz
     */
    suspend fun complete(id: UUID, isStopped: Boolean)
}
