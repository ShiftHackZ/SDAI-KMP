package com.shifthackz.aisdv1.presentation.generation.preview.img2img

import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageState

/**
 * Captured image-generation form and source-image availability for the preview picker.
 *
 * @param name Identifies the provider, source acquisition or validation condition.
 * @param state Fixed image-generation inputs, mask and status feedback.
 * @param anchor Logical base/advanced field key to bring into the viewport.
 * @param isFormExpanded Initial generation-options disclosure state.
 * @param isSourceUnavailable Whether retained encoded source data has no decoded preview.
 * @author Dmitriy Moroz
 */
internal data class ImageGenerationPreviewCase(
    val name: String,
    val state: ImageToImageState,
    val anchor: String = "",
    val isFormExpanded: Boolean = true,
    val isSourceUnavailable: Boolean = false,
)
