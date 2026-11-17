package com.shifthackz.aisdv1.domain.gateway

/**
 * Keeps a validated owned model available until selection persistence completes or fails.
 * Deletion must fail as busy while this lease is open. Callers must close it in finally,
 * including cancellation and configuration failures; closing never changes model selection.
 *
 * @author Dmitriy Moroz
 */
interface MnnModelSelectionLease {
    /**
     * SHA256 of the exact validated manifest whose root remains protected by this lease.
     *
     * @author Dmitriy Moroz
     */
    val activationId: String

    /**
     * Releases model and selection protection exactly once; repeated calls are harmless.
     *
     * @author Dmitriy Moroz
     */
    fun close()
}
