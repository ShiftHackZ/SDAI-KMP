package com.shifthackz.aisdv1.feature.onnx.environment

import ai.onnxruntime.OrtSession

/**
 * Keeps session options alive until their native session is disposed on its owner thread.
 *
 * @param value Native session transferred by the factory.
 * @param options Options used to create the session, including any provider-owned resources.
 * @author Dmitriy Moroz
 */
internal class OwnedOnnxSession(
    override val value: OrtSession,
    private val options: OrtSession.SessionOptions,
) : OnnxSession {
    private var isClosed = false

    /**
     * Closes each resource once, including options when session disposal fails.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        if (isClosed) return
        isClosed = true
        try {
            value.close()
        } finally {
            options.close()
        }
    }
}
