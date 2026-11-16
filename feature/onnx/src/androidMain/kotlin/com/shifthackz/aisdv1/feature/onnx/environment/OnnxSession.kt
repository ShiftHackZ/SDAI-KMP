package com.shifthackz.aisdv1.feature.onnx.environment

import ai.onnxruntime.OrtSession

/**
 * Owns a native session together with the options that must outlive its execution.
 * All access and disposal belong to the inference thread; cancellation must not close this owner.
 *
 * @author Dmitriy Moroz
 */
internal interface OnnxSession : AutoCloseable {
    /**
     * Native session borrowed until this owner closes; callers must not close it directly.
     *
     * @author Dmitriy Moroz
     */
    val value: OrtSession

    /**
     * Releases the session before its options, after all borrowed inputs and results are consumed.
     * Repeated disposal is safe.
     *
     * @author Dmitriy Moroz
     */
    override fun close()
}
