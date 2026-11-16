package com.shifthackz.aisdv1.feature.onnx.environment

/**
 * Job-scoped configuration; no inference component reads mutable application preferences.
 *
 * @param modelDirectory Captured readable ONNX model root.
 * @param isNnapiEnabled Submitted provider choice.
 * @param isDiagnosticsEnabled Whether native graph assignment logs are enabled for a development build.
 * @author Dmitriy Moroz
 */
internal data class OnnxRuntimeConfiguration(
    val modelDirectory: String,
    val isNnapiEnabled: Boolean,
    val isDiagnosticsEnabled: Boolean,
)
