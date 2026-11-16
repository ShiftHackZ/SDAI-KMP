package com.shifthackz.aisdv1.domain.entity

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/** Identifies the Android worker or iOS background job owning a remote operation; never contains prompts or images.
 * @param id Exact background job UUID used to reject delayed cancellation from an earlier worker.
 * @author Dmitriy Moroz */
class EasyDiffusionWorkOwner(val id: String) : AbstractCoroutineContextElement(Key) {
    /** Context key used by the domain operation at the beginning of dispatch.
     * @author Dmitriy Moroz */
    companion object Key : CoroutineContext.Key<EasyDiffusionWorkOwner>
}
