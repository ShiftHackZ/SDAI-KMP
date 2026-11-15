package com.shifthackz.aisdv1.presentation.widget.work

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationFailure

/**
 * Maps controlled failure categories and the attempted engine/backend to localized recovery text.
 * Arbitrary native exception messages and current provider selection never enter this description.
 *
 * @receiver Failure captured for the failed attempt.
 * @author Dmitriy Moroz
 */
internal fun NativeGenerationFailure.toUserMessage(): String {
    val engineName = when (engine) {
        NativeGenerationEngine.ONNX -> "ONNX"
        NativeGenerationEngine.MEDIA_PIPE -> "MediaPipe"
        NativeGenerationEngine.SDCPP -> "SDCPP"
        NativeGenerationEngine.BONSAI -> "Bonsai"
        null -> Localization.string("native_generation_engine_unknown")
    }
    val reasonKey = when (category) {
        NativeGenerationFailure.Category.BUSY -> "native_generation_busy"
        NativeGenerationFailure.Category.CONNECTION -> "native_generation_connection"
        NativeGenerationFailure.Category.PROCESS_DIED -> "native_generation_process_died"
        NativeGenerationFailure.Category.UNRESPONSIVE -> "native_generation_unresponsive"
        NativeGenerationFailure.Category.ENGINE -> "native_generation_engine_failed"
        NativeGenerationFailure.Category.INVALID_RESULT -> "native_generation_invalid_result"
        NativeGenerationFailure.Category.INTERRUPTED -> "native_generation_interrupted"
    }
    val backendName = backend?.displayName ?: bonsaiBackend?.displayName
    val attemptedEngine = backendName?.let { "$engineName / $it" } ?: engineName
    return Localization.string(
        "native_generation_failure_context",
        attemptedEngine,
        Localization.string(reasonKey)
    )
}
