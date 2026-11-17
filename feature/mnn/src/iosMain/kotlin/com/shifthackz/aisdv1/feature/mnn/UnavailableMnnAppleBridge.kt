package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest

/**
 * Completes explicitly unavailable until the application host injects its Apple runtime.
 *
 * @author Dmitriy Moroz
 */
internal class UnavailableMnnAppleBridge : MnnAppleBridge {
    /**
     * Rejects generation without retaining inputs or loading native libraries.
     *
     * @param ownerId Captured operation identity, unused because no work starts.
     * @param request Captured model inputs, never read by an unavailable host.
     * @param onProgress No progress is emitted for rejected work.
     * @param completion Receives exactly one unavailable result synchronously.
     *
     * @author Dmitriy Moroz
     */
    override fun generate(
        ownerId: String, request: NativeGenerationRequest,
        onProgress: (Int) -> Unit, completion: (MnnAppleResult) -> Unit,
    ) = completion(MnnAppleResult(null, "MNN Apple host is unavailable", false))

    /**
     * Cancellation is harmless because unavailable hosts own no executing work.
     *
     * @param ownerId Captured identity with no native operation to cancel.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt(ownerId: String) = Unit
}
