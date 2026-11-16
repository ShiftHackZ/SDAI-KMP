package com.shifthackz.aisdv1.work.mappers

import com.shifthackz.aisdv1.domain.entity.FalAiAcceleration
import com.shifthackz.aisdv1.domain.entity.FalAiImageSize
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import kotlinx.serialization.Serializable

/**
 * Serializes a complete image-generation snapshot, including captured source identity, for durable background work.
 *
 * @author Dmitriy Moroz
 */
internal fun ImageToImagePayload.toByteArray(): ByteArray {
    return payloadJson
        .encodeToString(ImageToImagePayloadDto.from(this))
        .encodeToByteArray()
}

/**
 * Decodes current or legacy work bytes; invalid or unknown source identifiers reject the payload by returning null.
 *
 * @author Dmitriy Moroz
 */
internal fun ByteArray.toImageToImagePayload(): ImageToImagePayload? {
    return runCatching {
        payloadJson
            .decodeFromString<ImageToImagePayloadDto>(decodeToString())
            .toPayload()
    }.getOrNull()
}

/**
 * Serializable background-work snapshot. Optional added identity fields preserve decoding of older queued jobs.
 *
 * @param sourceSnapshot Provider captured at submission; null preserves legacy queued-work routing.
 * @param providerSelectionRevision Durable source-selection revision used to reject stale queued work.
 * @param base64Image Encoded source image bytes for image-to-image generation.
 * @param base64MaskImage Encoded inpainting mask; empty for providers without inpainting.
 * @param denoisingStrength Image-to-image denoising strength in the range zero through one.
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
 * @author Dmitriy Moroz
 */
@Serializable
private data class ImageToImagePayloadDto(
    val sourceSnapshot: String? = null,
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
    val scheduler: String? = null,
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
    val stabilityAiClipGuidance: String?,
    /**
     * Optional Stability AI style preset.
     *
     * @author Dmitriy Moroz
     */
    val stabilityAiStylePreset: String?,
    /**
     * AUTOMATIC1111 ADetailer extension configuration.
     *
     * @author Dmitriy Moroz
     */
    val aDetailer: ADetailerConfigDto = ADetailerConfigDto(),
    /**
     * Selected Fal.ai image-generation endpoint.
     *
     * @author Dmitriy Moroz
     */
    val falAiModel: String? = null,
    /**
     * Fal.ai output-size preset.
     *
     * @author Dmitriy Moroz
     */
    val falAiImageSize: String? = null,
    /**
     * Fal.ai acceleration preset.
     *
     * @author Dmitriy Moroz
     */
    val falAiAcceleration: String? = null,
    /**
     * Whether a supporting Fal.ai endpoint requests synchronous output.
     *
     * @author Dmitriy Moroz
     */
    val falAiSyncMode: Boolean = false,
) {
    /**
     * Rebuilds domain inputs while validating any persisted source key against the current provider catalog.
     *
     * @author Dmitriy Moroz
     */
    fun toPayload(): ImageToImagePayload = ImageToImagePayload(
        sourceSnapshot = sourceSnapshot?.let { key -> requireNotNull(ServerSource.entries.firstOrNull { it.key == key }) },
        providerSelectionRevision = providerSelectionRevision,
        base64Image = base64Image,
        base64MaskImage = base64MaskImage,
        denoisingStrength = denoisingStrength,
        prompt = prompt,
        negativePrompt = negativePrompt,
        samplingSteps = samplingSteps,
        cfgScale = cfgScale,
        width = width,
        height = height,
        restoreFaces = restoreFaces,
        seed = seed,
        subSeed = subSeed,
        subSeedStrength = subSeedStrength,
        sampler = sampler,
        scheduler = scheduler?.let(Scheduler::fromAlias) ?: Scheduler.AUTOMATIC,
        nsfw = nsfw,
        batchCount = batchCount,
        inPaintingMaskInvert = inPaintingMaskInvert,
        inPaintFullResPadding = inPaintFullResPadding,
        inPaintingFill = inPaintingFill,
        inPaintFullRes = inPaintFullRes,
        maskBlur = maskBlur,
        stabilityAiClipGuidance = stabilityAiClipGuidance.parseEnumOrNull<StabilityAiClipGuidance>(),
        stabilityAiStylePreset = stabilityAiStylePreset.parseEnumOrNull<StabilityAiStylePreset>(),
        aDetailer = aDetailer.toDomain(),
        falAiModel = FalAiModel.parse(falAiModel, FalAiModel.defaultImageToImage),
        falAiImageSize = FalAiImageSize.parse(falAiImageSize),
        falAiAcceleration = FalAiAcceleration.parse(falAiAcceleration),
        falAiSyncMode = falAiSyncMode,
    )

    /**
     * Constructs durable image-work DTOs from domain requests while preserving captured provider ownership.
     *
     * @author Dmitriy Moroz
     */
    companion object {
        /**
         * Copies domain inputs into the durable background-work representation without losing source ownership.
         *
         * @param payload Immutable supported generation inputs and captured provider identity.
         * @author Dmitriy Moroz
         */
        fun from(payload: ImageToImagePayload): ImageToImagePayloadDto = ImageToImagePayloadDto(
            sourceSnapshot = payload.sourceSnapshot?.key,
            providerSelectionRevision = payload.providerSelectionRevision,
            base64Image = payload.base64Image,
            base64MaskImage = payload.base64MaskImage,
            denoisingStrength = payload.denoisingStrength,
            prompt = payload.prompt,
            negativePrompt = payload.negativePrompt,
            samplingSteps = payload.samplingSteps,
            cfgScale = payload.cfgScale,
            width = payload.width,
            height = payload.height,
            restoreFaces = payload.restoreFaces,
            seed = payload.seed,
            subSeed = payload.subSeed,
            subSeedStrength = payload.subSeedStrength,
            sampler = payload.sampler,
            scheduler = payload.scheduler.alias,
            nsfw = payload.nsfw,
            batchCount = payload.batchCount,
            inPaintingMaskInvert = payload.inPaintingMaskInvert,
            inPaintFullResPadding = payload.inPaintFullResPadding,
            inPaintingFill = payload.inPaintingFill,
            inPaintFullRes = payload.inPaintFullRes,
            maskBlur = payload.maskBlur,
            stabilityAiClipGuidance = payload.stabilityAiClipGuidance?.name,
            stabilityAiStylePreset = payload.stabilityAiStylePreset?.name,
            aDetailer = ADetailerConfigDto.from(payload.aDetailer),
            falAiModel = payload.falAiModel.alias,
            falAiImageSize = payload.falAiImageSize.key,
            falAiAcceleration = payload.falAiAcceleration.key,
            falAiSyncMode = payload.falAiSyncMode,
        )
    }
}
