package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest

/**
 * Keeps the common MNN API resolvable while iOS excludes this Android-only provider.
 *
 * @author Dmitriy Moroz
 */
internal class NoOpMnnRuntime : MnnRuntime {
    /**
     * Rejects accidental execution rather than reporting an empty successful image.
     *
     * @param request Captured inputs that cannot execute on iOS.
     * @param onProgress Remains unused because no native attempt starts.
     *
     * @author Dmitriy Moroz
     */
    override fun generate(request: NativeGenerationRequest, onProgress: (Int) -> Unit): ByteArray =
        error("MNN requires Android arm64")

    /**
     * No native attempt or resource exists on this platform.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() = Unit
}
