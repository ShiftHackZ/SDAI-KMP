package com.shifthackz.aisdv1.feature.onnx.environment

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtSession

/**
 * Job-scoped control for cooperative ONNX termination and run-options ownership.
 *
 * @author Dmitriy Moroz
 */
internal interface OnnxRunController : AutoCloseable {
    /**
     * Signals termination without closing resources used by a native run.
     *
     * @author Dmitriy Moroz
     */
    fun cancel()

    /**
     * Throws cancellation between native stages.
     *
     * @author Dmitriy Moroz
     */
    fun checkActive()

    /**
     * Checks cancellation and runs the borrowed session using this job's reusable run options.
     * The caller owns the returned result and must consume and close it before closing inputs or sessions.
     *
     * @param session Borrowed owner-thread session.
     * @param inputs Borrowed tensors kept alive through result consumption.
     * @author Dmitriy Moroz
     */
    fun run(session: OrtSession, inputs: Map<String, OnnxTensor>): OrtSession.Result

    /**
     * Owner-thread close after every run has returned.
     *
     * @author Dmitriy Moroz
     */
    override fun close()
}
