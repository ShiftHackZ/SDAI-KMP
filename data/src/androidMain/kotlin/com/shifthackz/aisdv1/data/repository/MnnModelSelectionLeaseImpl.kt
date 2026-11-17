package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.gateway.MnnModelSelectionLease
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Retains both file-lock owners while domain commits the validated selection.
 *
 * @param activationId Exact manifest SHA256 validated under both locks.
 * @param payloadOwner Keeps the immutable files alive and excludes replacement.
 * @param selectionOwner Rejects deletion before it can write a pending-delete marker.
 *
 * @author Dmitriy Moroz
 */
internal class MnnModelSelectionLeaseImpl(
    override val activationId: String,
    private val payloadOwner: AutoCloseable,
    private val selectionOwner: AutoCloseable
) : MnnModelSelectionLease {
    private val isClosed = AtomicBoolean(false)

    /**
     * Releases payload protection before allowing deletion to enter its critical section.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                payloadOwner.close()
            } finally {
                selectionOwner.close()
            }
        }
    }
}
