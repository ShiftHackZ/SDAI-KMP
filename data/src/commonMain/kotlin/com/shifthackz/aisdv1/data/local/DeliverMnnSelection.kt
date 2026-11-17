package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.domain.gateway.MnnModelSelectionLease
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Retains cleanup ownership until a selection lease crosses the dispatch return boundary.
 * Prompt cancellation can discard the returned value; the producer still closes that lease.
 *
 * @param dispatcher Worker dispatcher used for full bundle validation.
 * @param acquire Acquires and validates a live selection lease on the worker.
 * @return Lease owned by the caller only after successful delivery.
 *
 * @author Dmitriy Moroz
 */
internal suspend fun deliverMnnSelection(
    dispatcher: CoroutineDispatcher,
    acquire: suspend () -> MnnModelSelectionLease,
): MnnModelSelectionLease {
    var pending: MnnModelSelectionLease? = null
    try {
        val delivered = withContext(dispatcher) { acquire().also { pending = it } }
        pending = null
        return delivered
    } catch (failure: Throwable) {
        try {
            pending?.close()
        } catch (cleanup: Throwable) {
            failure.addSuppressed(cleanup)
        }
        throw failure
    }
}
