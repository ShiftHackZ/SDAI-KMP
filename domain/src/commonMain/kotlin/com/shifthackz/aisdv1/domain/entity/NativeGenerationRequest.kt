package com.shifthackz.aisdv1.domain.entity

/**
 * Immutable model and execution inputs captured before submitting a local generation.
 * Paths are private transport data and must never enter diagnostics or Binder messages.
 *
 * @param engine Native runtime selected for this attempt.
 * @param payload Submitted image parameters; changes to preferences cannot alter this snapshot.
 * @param modelPath App-accessible model file or directory.
 * @param isNnapiEnabled Whether ONNX may use NNAPI. Android submissions below 8.1 (API 27)
 * always pass false regardless of the saved preference and use ONNX Runtime CPU execution.
 * A true value permits NNAPI device selection; it does not guarantee hardware acceleration.
 * @author Dmitriy Moroz
 */
data class NativeGenerationRequest(
    val engine: NativeGenerationEngine,
    val payload: TextToImagePayload,
    val modelPath: String,
    val isNnapiEnabled: Boolean = false,
)
