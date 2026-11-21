package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload

/**
 * Captures execution configuration before dispatch, independent of later preference edits.
 *
 * @param source Provider admitted for this request.
 * @return Local execution configuration, or None for remote providers.
 * @author Dmitriy Moroz
 */
internal fun TextToImagePayload.generationAcceleration(source: ServerSource): String =
    when (source) {
        ServerSource.LOCAL_MICROSOFT_ONNX -> "CPU"
        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> "GPU"
        ServerSource.LOCAL_MNN -> when (mnnBackend.key) {
            "opencl" -> "OpenCL"; "metal" -> "Metal"; else -> "CPU"
        }

        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> sdxlBackend.displayName
        ServerSource.LOCAL_APPLE_BONSAI -> bonsaiAcceleration(bonsaiBackend.displayName)
        ServerSource.LOCAL_APPLE_CORE_ML -> coreMlAcceleration()
        else -> "None"
    }

/**
 * Names the platform-specific Bonsai execution configuration.
 *
 * @param requestedMode Captured Android backend selection.
 * @author Dmitriy Moroz
 */
internal expect fun bonsaiAcceleration(requestedMode: String): String

/**
 * Names the compute-unit policy used by the platform Core ML runtime.
 *
 * @author Dmitriy Moroz
 */
internal expect fun coreMlAcceleration(): String
