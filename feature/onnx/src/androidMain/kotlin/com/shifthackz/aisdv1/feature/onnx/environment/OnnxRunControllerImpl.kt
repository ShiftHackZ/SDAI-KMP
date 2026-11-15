package com.shifthackz.aisdv1.feature.onnx.environment

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtSession
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException

/**
 * Coordinates cancellation with the lifetime of one reusable run-options object.
 * Only the inference owner closes this controller after every native run has returned.
 *
 * @author Dmitriy Moroz
 */
internal class OnnxRunControllerImpl : OnnxRunController {
    private val lock = Any()
    private val isCancelled = AtomicBoolean(false)
    private var options: OrtSession.RunOptions? = OrtSession.RunOptions()

    /**
     * Requests ONNX cooperative termination without destroying a session or mutex used by run.
     *
     * @author Dmitriy Moroz
     */
    override fun cancel() {
        synchronized(lock) {
            isCancelled.set(true)
            options?.setTerminate(true)
        }
    }

    /**
     * Throws between noninterruptible native stages when cancellation has been requested.
     *
     * @author Dmitriy Moroz
     */
    override fun checkActive() {
        if (isCancelled.get()) throw CancellationException("Local generation cancelled")
    }

    /**
     * Checks cancellation and runs the borrowed session using this job's reusable run options.
     * The caller owns the returned result and must consume and close it before closing inputs or sessions.
     *
     * @param session Session exclusively owned by the inference thread.
     * @param inputs Tensors whose owner keeps them alive until the result has been consumed.
     * @author Dmitriy Moroz
     */
    override fun run(session: OrtSession, inputs: Map<String, OnnxTensor>): OrtSession.Result {
        checkActive()
        val activeOptions = synchronized(lock) { checkNotNull(options) }
        return session.run(inputs, activeOptions)
    }

    /**
     * Called by the inference owner after all runs returned, never by cancellation callbacks.
     *
     * @author Dmitriy Moroz
     */
    override fun close() = synchronized(lock) { options?.close(); options = null }
}
