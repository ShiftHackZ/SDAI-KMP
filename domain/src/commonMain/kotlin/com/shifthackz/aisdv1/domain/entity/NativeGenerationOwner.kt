package com.shifthackz.aisdv1.domain.entity

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * WorkManager identity propagated through sequential native batch requests.
 * A recreated worker repeats the same keys so the bounded attempt journal can reject a recorded
 * attempt while its entry remains retained.
 *
 * @param id Work request UUID, containing no generation inputs.
 * @author Dmitriy Moroz
 */
class NativeGenerationOwner(private val id: String) : AbstractCoroutineContextElement(Key) {
    private var index = 0

    /**
     * Allocates the next sequential image identity within this worker invocation.
     *
     * @author Dmitriy Moroz
     */
    fun nextKey(): String = "$id:${index++}"

    /**
     * Context lookup key.
     *
     * @author Dmitriy Moroz
     */
    companion object Key : CoroutineContext.Key<NativeGenerationOwner>
}
