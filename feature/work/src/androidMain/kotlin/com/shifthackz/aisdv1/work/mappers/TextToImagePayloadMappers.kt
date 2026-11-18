package com.shifthackz.aisdv1.work.mappers

import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.FalAiAcceleration
import com.shifthackz.aisdv1.domain.entity.FalAiImageSize
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * JSON codec for background txt2img payload handoff.
 *
 * Unknown keys stay ignored so queued work from an older app version can still
 * be read after new provider fields, such as local runtime backend choices, are
 * added to the DTO.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalSerializationApi::class)
internal val payloadJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/**
 * Serializes the domain payload for WorkManager cache storage.
 *
 * @author Dmitriy Moroz
 */
internal fun TextToImagePayload.toByteArray(): ByteArray {
    return payloadJson
        .encodeToString(TextToImagePayloadDto.from(this))
        .encodeToByteArray()
}

/**
 * Restores a cached WorkManager payload into the domain request model.
 *
 * @author Dmitriy Moroz
 */
internal fun ByteArray.toTextToImagePayload(): TextToImagePayload? {
    return runCatching {
        payloadJson
            .decodeFromString<TextToImagePayloadDto>(decodeToString())
            .toPayload()
    }.getOrNull()
}

/**
 * Stable serialized form for txt2img background work.
 *
 * The DTO stores provider-specific options as strings so enum aliases can be
 * parsed with backwards-compatible defaults when app versions change.
 *
 * @param prompt Positive prompt describing the requested image.
 * @param negativePrompt Prompt describing content the selected provider should avoid.
 * @param samplingSteps Requested denoising iteration count, subject to the provider profile.
 * @param cfgScale Classifier-free guidance strength requested from compatible providers.
 * @param width Requested output width in pixels.
 * @param height Requested output height in pixels.
 * @param restoreFaces Whether providers supporting face restoration should apply it after generation.
 * @param seed Seed editor value; providers resolve blank or random-seed conventions before execution.
 * @param subSeed Variation seed editor value for providers supporting seed interpolation.
 * @param subSeedStrength Interpolation strength between the primary seed and variation seed.
 * @param sampler Provider sampler identifier captured with this request.
 * @param scheduler Serialized noise scheduling strategy selected for compatible diffusion providers.
 * @param nsfw Whether the selected provider permits adult-content generation.
 * @param batchCount Number of images requested, subject to provider batching capabilities.
 * @param style Provider-specific image style value, or null when unused.
 * @param quality Provider-specific image quality value, or null when unused.
 * @param openAiModel Serialized openAI image model selected for providers using the built-in OpenAI catalog.
 * @param openAiCompatibleModel Arbitrary compatible-server image model identifier, independent of the built-in OpenAI catalog.
 * @param sourceSnapshot Persisted ServerSource.key; null routes older jobs using the selected source.
 * @param providerSelectionRevision Persisted source revision; null identifies legacy jobs rejected for native OAuth.
 * @param openAiOAuthAccountId Token-free account identifier; null for older/non-OAuth jobs.
 * @param openAiOAuthRevision Stable login revision; null for older/non-OAuth jobs, never an access token.
 * @param stabilityAiClipGuidance Serialized optional Stability AI CLIP guidance preset.
 * @param stabilityAiStylePreset Serialized optional Stability AI image style preset.
 * @param aDetailer AUTOMATIC1111 detail-refinement configuration; disabled for providers without this extension.
 * @param hires AUTOMATIC1111 high-resolution second-pass configuration.
 * @param forgeModules Forge modules selected for compatible AUTOMATIC1111 servers.
 * @param falAiModel Serialized fal AI endpoint defining the generation model and supported controls.
 * @param falAiImageSize Serialized fal AI image-size preset sent to the selected endpoint.
 * @param falAiAcceleration Serialized fal AI acceleration policy supported by the endpoint.
 * @param sdxlBackend Serialized requested stable-diffusion.cpp execution backend, including automatic selection.
 * @param bonsaiBackend Serialized requested Bonsai execution backend on supported platforms.
 * @param mnnBackend Explicit CPU or OpenCL choice retained for queued MNN work.
 * @param mnnActivationId Exact validated MNN manifest SHA256 captured with the submitted model.
 * @param mnnModelId Catalog or custom identity resolved to the captured MNN artifact.
 * @param mnnProfile Fixed MNN execution profile required when the queued job is admitted.
 * @param falAiSyncMode Whether Fal AI should return the image synchronously instead of using queued delivery.
 *
 * @author Dmitriy Moroz
 */
@Serializable
private data class TextToImagePayloadDto(
    val prompt: String,
    val negativePrompt: String,
    val samplingSteps: Int,
    val cfgScale: Float,
    val width: Int,
    val height: Int,
    val restoreFaces: Boolean,
    val seed: String,
    val subSeed: String,
    val subSeedStrength: Float,
    val sampler: String,
    val scheduler: String? = null,
    val nsfw: Boolean,
    val batchCount: Int,
    val style: String?,
    val quality: String?,
    val openAiModel: String?,
    /**
     * Arbitrary compatible-server model retained across the WorkManager handoff.
     *
     * @author Dmitriy Moroz
     */
    val openAiCompatibleModel: String = "gpt-image-2",
    val sourceSnapshot: String? = null,
    val providerSelectionRevision: Long? = null,
    val openAiOAuthAccountId: String? = null,
    val openAiOAuthRevision: String? = null,
    val stabilityAiClipGuidance: String?,
    val stabilityAiStylePreset: String?,
    val aDetailer: ADetailerConfigDto = ADetailerConfigDto(),
    val hires: HiresConfigDto = HiresConfigDto(),
    val forgeModules: List<ForgeModuleDto> = emptyList(),
    val falAiModel: String? = null,
    val falAiImageSize: String? = null,
    val falAiAcceleration: String? = null,
    val sdxlBackend: String? = null,
    val bonsaiBackend: String? = null,
    val mnnBackend: String? = null,
    val mnnActivationId: String? = null,
    val mnnModelId: String? = null,
    val mnnProfile: String? = null,
    val falAiSyncMode: Boolean = false,
) {
    fun toPayload(): TextToImagePayload = TextToImagePayload(
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
        style = style,
        quality = quality,
        openAiModel = openAiModel?.let(OpenAiModel::parse),
        openAiCompatibleModel = openAiCompatibleModel,
        sourceSnapshot = sourceSnapshot?.let { key -> requireNotNull(ServerSource.entries.firstOrNull { it.key == key }) },
        openAiOAuthAccountId = openAiOAuthAccountId,
        providerSelectionRevision = providerSelectionRevision,
        openAiOAuthRevision = openAiOAuthRevision,
        stabilityAiClipGuidance = stabilityAiClipGuidance.parseEnumOrNull<StabilityAiClipGuidance>(),
        stabilityAiStylePreset = stabilityAiStylePreset.parseEnumOrNull<StabilityAiStylePreset>(),
        aDetailer = aDetailer.toDomain(),
        hires = hires.toDomain(),
        forgeModules = forgeModules.map(ForgeModuleDto::toDomain),
        falAiModel = FalAiModel.parse(falAiModel, FalAiModel.defaultTextToImage),
        falAiImageSize = FalAiImageSize.parse(falAiImageSize),
        falAiAcceleration = FalAiAcceleration.parse(falAiAcceleration),
        sdxlBackend = SdxlBackend.parse(sdxlBackend),
        bonsaiBackend = BonsaiBackend.parse(bonsaiBackend),
        mnnBackend = mnnBackend?.let { key -> requireNotNull(MnnBackend.entries.firstOrNull { it.key == key }) }
            ?: MnnBackend.CPU,
        mnnActivationId = mnnActivationId,
        mnnModelId = mnnModelId,
        mnnProfile = mnnProfile,
        falAiSyncMode = falAiSyncMode,
    )

    companion object {
        fun from(payload: TextToImagePayload): TextToImagePayloadDto = TextToImagePayloadDto(
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
            style = payload.style,
            quality = payload.quality,
            openAiModel = payload.openAiModel?.alias,
            openAiCompatibleModel = payload.openAiCompatibleModel,
            sourceSnapshot = payload.sourceSnapshot?.key,
            providerSelectionRevision = payload.providerSelectionRevision,
            openAiOAuthAccountId = payload.openAiOAuthAccountId,
            openAiOAuthRevision = payload.openAiOAuthRevision,
            stabilityAiClipGuidance = payload.stabilityAiClipGuidance?.name,
            stabilityAiStylePreset = payload.stabilityAiStylePreset?.name,
            aDetailer = ADetailerConfigDto.from(payload.aDetailer),
            hires = HiresConfigDto.from(payload.hires),
            forgeModules = payload.forgeModules.map(ForgeModuleDto::from),
            falAiModel = payload.falAiModel.alias,
            falAiImageSize = payload.falAiImageSize.key,
            falAiAcceleration = payload.falAiAcceleration.key,
            sdxlBackend = payload.sdxlBackend.key,
            bonsaiBackend = payload.bonsaiBackend.key,
            mnnBackend = payload.mnnBackend.key,
            mnnActivationId = payload.mnnActivationId,
            mnnModelId = payload.mnnModelId,
            mnnProfile = payload.mnnProfile,
            falAiSyncMode = payload.falAiSyncMode,
        )
    }
}

/**
 * Parses enum names from cached payload fields while keeping unknown values nullable.
 *
 * @author Dmitriy Moroz
 */
internal inline fun <reified T : Enum<T>> String?.parseEnumOrNull(): T? {
    return this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
}
