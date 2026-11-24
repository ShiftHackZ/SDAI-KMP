package com.shifthackz.aisdv1.feature.mnn

/**
 * Owns one tiled MNN super-resolution attempt inside the isolated inference process.
 *
 * @author Dmitriy Moroz
 */
internal interface MnnUpscalerNativeBridge {
    /**
     * Runs normalized RGB inference and returns packed RGB output with four times each source edge.
     *
     * @param modelPath Completed app-owned MNN checkpoint.
     * @param pixels Packed RGB source pixels.
     * @param width Source width.
     * @param height Source height.
     * @param onProgress Completed tile percentage.
     * @author Dmitriy Moroz
     */
    fun run(
        modelPath: String,
        pixels: ByteArray,
        width: Int,
        height: Int,
        onProgress: (Int) -> Unit
    ): ByteArray

    /**
     * Requests interruption without destroying the executing session.
     *
     * @author Dmitriy Moroz
     */
    fun interrupt()

    /**
     * Releases the handle after execution and cancellation have drained.
     *
     * @author Dmitriy Moroz
     */
    fun close()
}
