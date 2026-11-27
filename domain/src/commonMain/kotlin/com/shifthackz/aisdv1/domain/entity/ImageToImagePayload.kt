package com.shifthackz.aisdv1.domain.entity

/**
 * Immutable superset of image-generation inputs; providers consume only supported controls and queued work retains its captured source revision.
 *
 * @param sourceSnapshot Provider captured at submission; null preserves legacy queued-work routing.
 * @param providerSelectionRevision Durable provider/connection revision used to reject stale queued work.
 * @param base64Image Encoded source image bytes for image-to-image generation.
 * @param base64MaskImage Encoded inpainting mask; empty for providers without inpainting.
 * @param denoisingStrength Image-to-image denoising strength in the range zero through one.
 * @param upscaler Independent provider and model for optional final super-resolution; null leaves generation output unchanged.
 * @param prompt Positive generation prompt, preserved verbatim.
 * @param negativePrompt Negative generation prompt; unsupported providers omit it.
 * @param samplingSteps Requested inference step count.
 * @param cfgScale Requested classifier-free guidance scale.
 * @param width Requested output width in pixels.
 * @param height Requested output height in pixels.
 * @param restoreFaces Whether a supporting provider applies face restoration.
 * @param seed Decimal seed or the normal blank/-1 random sentinel.
 * @param subSeed Variation seed for supporting providers only.
 * @param subSeedStrength Variation seed blend strength for supporting providers.
 * @param sampler Provider-specific sampler identifier.
 * @param scheduler Provider-specific scheduling strategy; AUTOMATIC preserves normal selection.
 * @param nsfw Provider-specific safety-filter preference.
 * @param batchCount Number of requested images; providers may execute the batch sequentially.
 * @param inPaintingMaskInvert Provider-specific mask inversion setting.
 * @param inPaintFullResPadding Pixel padding around an inpainting crop.
 * @param inPaintingFill Provider-specific masked-region fill strategy.
 * @param inPaintFullRes Whether a supporting provider inpaints using a full-resolution crop.
 * @param maskBlur Inpainting mask blur radius in pixels.
 * @param stabilityAiClipGuidance Optional Stability AI CLIP guidance preset.
 * @param stabilityAiStylePreset Optional Stability AI style preset.
 * @param aDetailer AUTOMATIC1111 ADetailer extension configuration.
 * @param falAiModel Selected Fal.ai image-generation endpoint.
 * @param falAiImageSize Fal.ai output-size preset.
 * @param falAiAcceleration Fal.ai acceleration preset.
 * @param falAiSyncMode Whether a supporting Fal.ai endpoint requests synchronous output.
 * @param arliAiModel Selected ArliAI checkpoint identifier.
 * @author Dmitriy Moroz
 */
data class ImageToImagePayload(
    val sourceSnapshot: ServerSource? = null,
    val providerSelectionRevision: Long? = null,
    /**
     * Encoded source image bytes for image-to-image generation.
     *
     * @author Dmitriy Moroz
     */
    val base64Image: String,
    /**
     * Encoded inpainting mask; empty for providers without inpainting.
     *
     * @author Dmitriy Moroz
     */
    val base64MaskImage: String,
    /**
     * Image-to-image denoising strength in the range zero through one.
     *
     * @author Dmitriy Moroz
     */
    val denoisingStrength: Float,
    /**
     * Positive generation prompt, preserved verbatim.
     *
     * @author Dmitriy Moroz
     */
    val prompt: String,
    /**
     * Negative generation prompt; unsupported providers omit it.
     *
     * @author Dmitriy Moroz
     */
    val negativePrompt: String,
    /**
     * Requested inference step count.
     *
     * @author Dmitriy Moroz
     */
    val samplingSteps: Int,
    /**
     * Requested classifier-free guidance scale.
     *
     * @author Dmitriy Moroz
     */
    val cfgScale: Float,
    /**
     * Requested output width in pixels.
     *
     * @author Dmitriy Moroz
     */
    val width: Int,
    /**
     * Requested output height in pixels.
     *
     * @author Dmitriy Moroz
     */
    val height: Int,
    /**
     * Whether a supporting provider applies face restoration.
     *
     * @author Dmitriy Moroz
     */
    val restoreFaces: Boolean,
    /**
     * Decimal seed or the normal blank/-1 random sentinel.
     *
     * @author Dmitriy Moroz
     */
    val seed: String,
    /**
     * Variation seed for supporting providers only.
     *
     * @author Dmitriy Moroz
     */
    val subSeed: String,
    /**
     * Variation seed blend strength for supporting providers.
     *
     * @author Dmitriy Moroz
     */
    val subSeedStrength: Float,
    /**
     * Provider-specific sampler identifier.
     *
     * @author Dmitriy Moroz
     */
    val sampler: String,
    /**
     * Provider-specific scheduling strategy; AUTOMATIC preserves normal selection.
     *
     * @author Dmitriy Moroz
     */
    val scheduler: Scheduler = Scheduler.AUTOMATIC,
    /**
     * Provider-specific safety-filter preference.
     *
     * @author Dmitriy Moroz
     */
    val nsfw: Boolean,
    /**
     * Number of requested images; providers may execute the batch sequentially.
     *
     * @author Dmitriy Moroz
     */
    val batchCount: Int,
    /**
     * Provider-specific mask inversion setting.
     *
     * @author Dmitriy Moroz
     */
    val inPaintingMaskInvert: Int,
    /**
     * Pixel padding around an inpainting crop.
     *
     * @author Dmitriy Moroz
     */
    val inPaintFullResPadding: Int,
    /**
     * Provider-specific masked-region fill strategy.
     *
     * @author Dmitriy Moroz
     */
    val inPaintingFill: Int,
    /**
     * Whether a supporting provider inpaints using a full-resolution crop.
     *
     * @author Dmitriy Moroz
     */
    val inPaintFullRes: Boolean,
    /**
     * Inpainting mask blur radius in pixels.
     *
     * @author Dmitriy Moroz
     */
    val maskBlur: Int,
    /**
     * Optional Stability AI CLIP guidance preset.
     *
     * @author Dmitriy Moroz
     */
    val stabilityAiClipGuidance: StabilityAiClipGuidance?,
    /**
     * Optional Stability AI style preset.
     *
     * @author Dmitriy Moroz
     */
    val stabilityAiStylePreset: StabilityAiStylePreset?,
    /**
     * AUTOMATIC1111 ADetailer extension configuration.
     *
     * @author Dmitriy Moroz
     */
    val aDetailer: ADetailerConfig = ADetailerConfig.DISABLED,
    /**
     * Selected Fal.ai image-generation endpoint.
     *
     * @author Dmitriy Moroz
     */
    val falAiModel: FalAiModel = FalAiModel.defaultImageToImage,
    /**
     * Fal.ai output-size preset.
     *
     * @author Dmitriy Moroz
     */
    val falAiImageSize: FalAiImageSize = FalAiImageSize.default,
    /**
     * Fal.ai acceleration preset.
     *
     * @author Dmitriy Moroz
     */
    val falAiAcceleration: FalAiAcceleration = FalAiAcceleration.default,
    /**
     * Whether a supporting Fal.ai endpoint requests synchronous output.
     *
     * @author Dmitriy Moroz
     */
    val falAiSyncMode: Boolean = false,
    /**
     * Selected ArliAI checkpoint identifier.
     *
     * @author Dmitriy Moroz
     */
    val arliAiModel: String = "",
    val upscaler: UpscalerConfiguration? = null,
)
