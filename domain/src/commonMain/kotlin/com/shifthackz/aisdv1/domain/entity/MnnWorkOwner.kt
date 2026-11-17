package com.shifthackz.aisdv1.domain.entity

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Captures the iOS background operation whose interruption may reach a native attempt.
 *
 * @param id Exact scheduled operation UUID; delayed cancellations cannot target a successor.
 *
 * @author Dmitriy Moroz
 */
class MnnWorkOwner(val id: String) : AbstractCoroutineContextElement(Key) {
    /** Lookup key retained across the non-cancellable native drain.
     *
     * @author Dmitriy Moroz
     */
    companion object Key : CoroutineContext.Key<MnnWorkOwner>
}
