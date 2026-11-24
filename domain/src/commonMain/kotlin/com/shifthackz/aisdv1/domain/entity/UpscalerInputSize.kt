package com.shifthackz.aisdv1.domain.entity

/**
 * Maximum prepared source edge accepted by the local upscaler, in pixels.
 *
 * @author Dmitriy Moroz
 */
const val MAX_UPSCALER_INPUT_SIZE = 1024

/**
 * Whether the requested output, including A1111 Hires.Fix, fits the optional upscaler's input.
 * Disabled upscaling imposes no additional generation-size restriction.
 *
 * @receiver Immutable text-generation submission.
 * @author Dmitriy Moroz
 */
val TextToImagePayload.isUpscalerInputSizeValid: Boolean
    get() {
        val scale = if (hires.enabled) hires.scale else 1f
        return upscaler == null || (width * scale <= MAX_UPSCALER_INPUT_SIZE && height * scale <= MAX_UPSCALER_INPUT_SIZE)
    }

/**
 * Whether requested image-generation dimensions fit the enabled final upscaler stage.
 *
 * @receiver Immutable image-generation submission.
 * @author Dmitriy Moroz
 */
val ImageToImagePayload.isUpscalerInputSizeValid: Boolean
    get() = upscaler == null || (width <= MAX_UPSCALER_INPUT_SIZE && height <= MAX_UPSCALER_INPUT_SIZE)
