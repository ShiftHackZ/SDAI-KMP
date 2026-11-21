package com.shifthackz.aisdv1.domain.entity

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Carries request identity and a caller-owned progress sink through transport and sequential batches.
 *
 * @param taskId Unique AUTOMATIC1111 task identity; never shared with another submission.
 * @param report Receives a measured fraction, or null when no measurement is available.
 * @author Dmitriy Moroz
 */
class RemoteGenerationProgressContext(
    val taskId: String,
    val report: (Float?) -> Unit,
) : AbstractCoroutineContextElement(Key) {
    /**
     * Coroutine lookup key for the current submission.
     *
     * @author Dmitriy Moroz
     */
    companion object Key : CoroutineContext.Key<RemoteGenerationProgressContext>
}
