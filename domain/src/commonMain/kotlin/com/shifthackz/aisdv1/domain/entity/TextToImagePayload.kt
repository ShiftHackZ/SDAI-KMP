package com.shifthackz.aisdv1.domain.entity

/**
 * Provider-neutral request model for text-to-image generation.
 *
 * The payload intentionally carries superset fields from remote APIs and local
 * runtimes. Repository and feature implementations pick the fields relevant to
 * their provider, including Android local runtime backend choices such as SDXL
 * and Bonsai.
 *
 * @param upscaler Independent provider and model for optional final super-resolution; null leaves generation output unchanged.
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
 * @param scheduler Noise scheduling strategy selected for compatible diffusion providers.
 * @param nsfw Whether the selected provider permits adult-content generation.
 * @param batchCount Number of images requested, subject to provider batching capabilities.
 * @param style Provider-specific image style value, or null when unused.
 * @param quality Provider-specific image quality value, or null when unused.
 * @param openAiModel OpenAI image model selected for providers using the built-in OpenAI catalog.
 * @param stabilityAiClipGuidance Optional Stability AI CLIP guidance preset.
 * @param stabilityAiStylePreset Optional Stability AI image style preset.
 * @param aDetailer AUTOMATIC1111 detail-refinement configuration; disabled for providers without this extension.
 * @param hires AUTOMATIC1111 high-resolution second-pass configuration.
 * @param forgeModules Forge modules selected for compatible AUTOMATIC1111 servers.
 * @param falAiModel Fal AI endpoint defining the generation model and supported controls.
 * @param falAiImageSize Fal AI image-size preset sent to the selected endpoint.
 * @param falAiAcceleration Fal AI acceleration policy supported by the endpoint.
 * @param sdxlBackend Requested stable-diffusion.cpp execution backend, including automatic selection.
 * @param bonsaiBackend Requested Bonsai execution backend on supported platforms.
 * @param mnnBackend Captured MNN backend; never reread from preferences during execution.
 * @param mnnActivationId SHA256 of the selected manifest, captured before queuing.
 * @param mnnModelId Captured catalog or custom snapshot identity used to reject stale queued work.
 * @param mnnProfile Exact validated MNN graph profile; absent for non-MNN and legacy work.
 * @param falAiSyncMode Whether Fal AI should return the image synchronously instead of using queued delivery.
 * @param arliAiModel Arli AI model identifier selected for image generation.
 * @param openAiCompatibleModel Arbitrary compatible-server image model identifier, independent of the built-in OpenAI catalog.
 * @param sourceSnapshot Source selected at submission; null preserves legacy queued-work routing.
 * @param providerSelectionRevision Durable provider/connection revision used to reject stale queued work.
 * @param openAiOAuthAccountId Token-free ChatGPT account identifier captured at submission.
 * @param openAiOAuthRevision Stable protected-session revision; replacement logins invalidate earlier work.
 *
 * @author Dmitriy Moroz
 */
data class TextToImagePayload(
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
    val scheduler: Scheduler = Scheduler.AUTOMATIC,
    val nsfw: Boolean,
    val batchCount: Int,
    val style: String?,
    val quality: String?,
    val openAiModel: OpenAiModel?,
    val stabilityAiClipGuidance: StabilityAiClipGuidance?,
    val stabilityAiStylePreset: StabilityAiStylePreset?,
    val aDetailer: ADetailerConfig = ADetailerConfig.DISABLED,
    val hires: HiresConfig = HiresConfig.DISABLED,
    val forgeModules: List<ForgeModule> = emptyList(),
    val falAiModel: FalAiModel = FalAiModel.defaultTextToImage,
    val falAiImageSize: FalAiImageSize = FalAiImageSize.default,
    val falAiAcceleration: FalAiAcceleration = FalAiAcceleration.default,
    val sdxlBackend: SdxlBackend = SdxlBackend.AUTO,
    val bonsaiBackend: BonsaiBackend = BonsaiBackend.AUTO,
    val mnnBackend: MnnBackend = MnnBackend.CPU,
    val mnnActivationId: String? = null,
    val mnnModelId: String? = null,
    val mnnProfile: String? = null,
    val falAiSyncMode: Boolean = false,
    val arliAiModel: String = "",
    /**
     * Arbitrary image model ID used only by OPEN_AI_COMPATIBLE.
     *
     * @author Dmitriy Moroz
     */
    val openAiCompatibleModel: String = "gpt-image-2",
    /**
     * Selected source at submission; null preserves legacy queued-work routing.
     *
     * @author Dmitriy Moroz
     */
    val sourceSnapshot: ServerSource? = null,
    /**
     * Durable provider selection revision captured with this work; null marks an older payload.
     *
     * @author Dmitriy Moroz
     */
    val providerSelectionRevision: Long? = null,
    /**
     * Token-free account identifier captured when OAuth work is submitted.
     *
     * @author Dmitriy Moroz
     */
    val openAiOAuthAccountId: String? = null,
    /**
     * Stable login revision; stale queued work must never use a replacement session.
     *
     * @author Dmitriy Moroz
     */
    val openAiOAuthRevision: String? = null,
    val upscaler: UpscalerConfiguration? = null,
)
