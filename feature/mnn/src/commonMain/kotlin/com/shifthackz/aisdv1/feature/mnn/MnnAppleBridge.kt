package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest

/**
 * Host-injected asynchronous Apple runtime; completion follows native drain.
 *
 * @author Dmitriy Moroz
 */
interface MnnAppleBridge {
    /**
     * Enqueues immutable inputs without blocking the main thread.
     *
     * @param ownerId Unique identity also used for queued cancellation.
     * @param request Validated captured model and generation parameters.
     * @param onProgress Monotonic overall percentage for this owner.
     * @param completion Exactly one result after native return and workspace cleanup.
     *
     * @author Dmitriy Moroz
     */
    fun generate(
        ownerId: String, request: NativeGenerationRequest,
        onProgress: (Int) -> Unit, completion: (MnnAppleResult) -> Unit
    )

    /**
     * Requests cooperative cancellation without freeing active resources.
     *
     * @param ownerId Exact queued or executing owner; stale cancellations are harmless.
     *
     * @author Dmitriy Moroz
     */
    fun interrupt(ownerId: String)
}
