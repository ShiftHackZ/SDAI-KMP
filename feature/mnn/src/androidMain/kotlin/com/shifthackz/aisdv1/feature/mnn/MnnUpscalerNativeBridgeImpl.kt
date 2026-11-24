package com.shifthackz.aisdv1.feature.mnn

/**
 * Loads the existing MNN binary only in the inference child and retains one native cancellation handle.
 *
 * @author Dmitriy Moroz
 */
internal class MnnUpscalerNativeBridgeImpl : MnnUpscalerNativeBridge {
    private val handle: Long
    private var callback: ((Int) -> Unit)? = null

    init {
        System.loadLibrary("sdai_mnn")
        handle = createNative()
        check(handle != 0L)
    }

    /**
     * Keeps the callback alive until native return; failures and cancellation are not hidden.
     *
     * @param modelPath Completed MNN graph.
     * @param pixels Packed RGB source.
     * @param width Source width.
     * @param height Source height.
     * @param onProgress Tile completion percentage.
     * @author Dmitriy Moroz
     */
    override fun run(
        modelPath: String,
        pixels: ByteArray,
        width: Int,
        height: Int,
        onProgress: (Int) -> Unit
    ): ByteArray {
        callback = onProgress
        return try {
            runNative(handle, modelPath, pixels, width, height)
        } finally {
            callback = null
        }
    }

    /**
     * Requests cooperative cancellation for this handle only.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() = interruptNative(handle)

    /**
     * Destroys the drained handle on its inference owner.
     *
     * @author Dmitriy Moroz
     */
    override fun close() = destroyNative(handle)

    /**
     * Receives native tile progress on the execution thread.
     *
     * @param percent Bounded completed-tile percentage.
     * @author Dmitriy Moroz
     */
    @Suppress("unused")
    fun onProgress(percent: Int) {
        callback?.invoke(percent)
    }

    private external fun createNative(): Long
    private external fun runNative(
        handle: Long,
        model: String,
        input: ByteArray,
        width: Int,
        height: Int
    ): ByteArray

    private external fun interruptNative(handle: Long)
    private external fun destroyNative(handle: Long)
}
