package com.shifthackz.aisdv1.domain.gateway

/**
 * Prevents provider/settings restore from invalidating active foreground or background generation.
 * @author Dmitriy Moroz
 */
interface GenerationRestoreGate {
    /**
     * Runs generation after any active restore has completed; cancellation while waiting propagates.
     * @param block Complete provider operation owned by one generation use case.
     * @author Dmitriy Moroz
     */
    suspend fun <T> generating(block: suspend () -> T): T

    /**
     * Rejects restore while generation owns the gate, then prevents new generation until restore finishes.
     * @param block Serialized restore operation, including conflict comparison and data commits.
     * @author Dmitriy Moroz
     */
    suspend fun <T> restoring(block: suspend () -> T): T
}
