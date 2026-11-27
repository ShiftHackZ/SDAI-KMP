package com.shifthackz.aisdv1.domain.entity

/**
 * Captured postprocessing selection, independent of the image generation provider.
 *
 * @param source Independent provider used for this final-stage request.
 * @param modelId Dedicated downloaded super-resolution model identity.
 * @param scaleFactor Requested final enlargement, including downsampling when below the native model factor.
 * @author Dmitriy Moroz
 */
data class UpscalerConfiguration(
    val modelId: String,
    val source: UpscalerSource = UpscalerSource.forModel(modelId),
    val scaleFactor: Int = source.nativeScaleFactor,
)
