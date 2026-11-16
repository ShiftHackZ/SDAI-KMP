package com.shifthackz.aisdv1.feature.onnx.environment

/**
 * Opens job-owned ORT models with consistent provider configuration and native resource ownership.
 *
 * @author Dmitriy Moroz
 */
internal interface OnnxSessionFactory {
    /**
     * Creates a session and permits NNAPI's CPU implementation for unsupported accelerator ops.
     * Model, storage, provider and cancellation failures propagate to the caller.
     *
     * @param modelFile Relative ORT model path under the captured model directory.
     * @param isAccelerationAllowed Whether this stage may use the submitted NNAPI setting.
     * @return Session owner that must be closed after its last native run.
     * @author Dmitriy Moroz
     */
    fun create(
        modelFile: String,
        isAccelerationAllowed: Boolean,
    ): OnnxSession
}
