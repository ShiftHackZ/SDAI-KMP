package com.shifthackz.aisdv1.feature.mnn

import java.io.Closeable

/**
 * Protects model bytes independently of the host connection until native readers have stopped.
 *
 * @author Dmitriy Moroz
 */
internal interface MnnModelLeaseOwner {
    /**
     * Acquires child ownership before checking availability and the captured activation marker.
     * The caller must retain the lease until every native model reader has been destroyed.
     *
     * @param modelPath Canonical app-owned root from the validated exchange request.
     * @param activationId Captured manifest SHA-256 expected in the completion marker.
     *
     * @author Dmitriy Moroz
     */
    fun acquire(modelPath: String, activationId: String): Closeable
}
