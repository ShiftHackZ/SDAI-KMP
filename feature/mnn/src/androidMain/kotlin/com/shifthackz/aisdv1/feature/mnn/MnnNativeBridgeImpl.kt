package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import java.io.File

/**
 * Loads the native library exclusively inside the child inference dependency graph.
 *
 * @param cacheOwner Reserves private per-attempt output, OpenCL cache and CPU mmap backing.
 * @param modelOwner Child-held model protection independent of host teardown confirmation.
 *
 * @author Dmitriy Moroz
 */
internal class MnnNativeBridgeImpl(
    private val cacheOwner: MnnCacheOwner,
    private val modelOwner: MnnModelLeaseOwner,
) : MnnRuntime {
    private var progressCallback: ((Int) -> Unit)? = null

    @Volatile
    private var isLoaded = false

    /**
     * Owns one temporary output directory until the completed bytes have been read.
     *
     * @param request Captured model and supported execution options.
     * @param onProgress Receives overall completion percentage on the native owner thread.
     *
     * @author Dmitriy Moroz
     */
    override fun generate(request: NativeGenerationRequest, onProgress: (Int) -> Unit): ByteArray {
        return modelOwner.acquire(
            request.modelPath,
            requireNotNull(request.payload.mnnActivationId)
        ).use {
            System.loadLibrary("sdai_mnn")
            isLoaded = true
            cacheOwner.acquire().use { attempt ->
                val output = File(attempt.directory, "result.png")
                progressCallback = onProgress
                try {
                    generateNative(
                        request.modelPath,
                        request.payload.prompt.encodeToByteArray(),
                        output.path,
                        File(attempt.directory, ".tempcache").path,
                        request.payload.samplingSteps,
                        request.payload.seed.toInt(),
                        request.payload.mnnBackend == MnnBackend.OPENCL
                    )
                    check(output.length() in 1..16_777_216)
                    output.readBytes()
                } finally {
                    progressCallback = null
                }
            }
        }
    }

    /**
     * Signals a native atomic flag; gateway termination bounds an unresponsive driver.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() {
        if (isLoaded) interruptNative()
    }

    /**
     * JNI callback reached synchronously while the owning runtime remains alive.
     *
     * @param percent Overall upstream percentage.
     *
     * @author Dmitriy Moroz
     */
    @Suppress("unused")
    fun onNativeProgress(percent: Int) {
        progressCallback?.invoke(percent.coerceIn(0, 100))
    }

    private external fun generateNative(
        model: String, prompt: ByteArray, output: String,
        cache: String, steps: Int, seed: Int, isOpenCl: Boolean
    )

    private external fun interruptNative()
}
