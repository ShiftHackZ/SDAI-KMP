package com.shifthackz.aisdv1.domain.entity

/**
 * Persisted gallery identity and generation parameters; originals are read explicitly by ID.
 *
 * @param id Persisted gallery identity.
 * @param imageRevision Opaque image content identity, independent of flags.
 * @param createdAt Creation time in epoch milliseconds.
 * @param type Generation operation that determines available tabs.
 * @param prompt Positive generation prompt.
 * @param negativePrompt Negative generation prompt.
 * @param width Original output width in pixels.
 * @param height Original output height in pixels.
 * @param samplingSteps Generation sampling step count.
 * @param cfgScale Classifier-free guidance scale.
 * @param shouldRestoreFaces Whether face restoration was requested.
 * @param sampler Selected sampling algorithm.
 * @param seed Provider seed text.
 * @param subSeed Variation seed text.
 * @param subSeedStrength Variation seed strength.
 * @param denoisingStrength Image-to-image denoising strength.
 * @param isHidden Whether grid/detail output is obscured.
 * @param isLiked Whether the gallery result is liked.
 * @param modelName Recorded model display name.
 * @author Dmitriy Moroz
 */
data class GalleryItemMetadata(
    val id: Long,
    val imageRevision: String,
    val createdAt: Long,
    val type: AiGenerationResult.Type,
    val prompt: String,
    val negativePrompt: String,
    val width: Int,
    val height: Int,
    val samplingSteps: Int,
    val cfgScale: Float,
    val shouldRestoreFaces: Boolean,
    val sampler: String,
    val seed: String,
    val subSeed: String,
    val subSeedStrength: Float,
    val denoisingStrength: Float,
    val isHidden: Boolean,
    val isLiked: Boolean,
    val modelName: String,
) {
    /**
     * Validated layout ratio for history thumbnails.
     *
     * @author Dmitriy Moroz
     */
    val aspectRatio: Float get() = if (width > 0 && height > 0) width.toFloat() / height else 1f
}
