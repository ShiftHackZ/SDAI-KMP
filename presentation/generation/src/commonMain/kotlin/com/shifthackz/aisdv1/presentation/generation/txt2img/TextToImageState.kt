package com.shifthackz.aisdv1.presentation.generation.txt2img

import kotlin.time.TimeMark
import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.FalAiAcceleration
import com.shifthackz.aisdv1.domain.entity.FalAiImageSize
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiQuality
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.generation.model.PromptTagEditRequest
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormState

/**
 * Complete render state for txt2img.
 * The state combines screen lifecycle flags, generated results, validation
 * feedback, current platform metadata, and the shared generation form contract
 * used by provider-specific controls such as Android Bonsai backend selection.
 *
 * @param loadingConfiguration Whether provider configuration is still being loaded into the screen.
 * @param generationStartedAt Monotonic foreground start retained across dialog stages and composition recreation.
 * @param generating Whether a generation request is currently in progress.
 * @param savingImage Whether an image export is running.
 * @param sharingImage Whether an image is being prepared for the platform share flow.
 * @param promptValidationError Inline prompt validation feedback, or null when the prompt is accepted.
 * @param error Current blocking generation error, or null when no error is displayed.
 * @param message Transient user-facing generation message, or null when absent.
 * @param screenModal Currently displayed generation dialog or sheet.
 * @param results Generated images available to the result pager and gallery actions.
 * @param editTag Prompt tag currently being edited, or null when the tag editor is closed.
 * @param onBoardingDemo Whether generation controls are being displayed as an onboarding demonstration.
 * @param platform Native platform used to choose supported controls and navigation behavior.
 * @param mode Currently selected image-generation provider.
 * @param advancedToggleButtonVisible Whether the user can expand the provider's advanced controls.
 * @param advancedOptionsVisible Whether advanced generation controls are currently expanded.
 * @param formPromptTaggedInput Whether prompts are edited as tags instead of plain text.
 * @param prompt Positive prompt describing the requested image.
 * @param negativePrompt Prompt describing content the selected provider should avoid.
 * @param width Width editor text in pixels; invalid values remain visible with inline feedback.
 * @param height Height editor text in pixels; invalid values remain visible with inline feedback.
 * @param samplingSteps Requested denoising iteration count, subject to the provider profile.
 * @param cfgScale Classifier-free guidance strength requested from compatible providers.
 * @param restoreFaces Whether providers supporting face restoration should apply it after generation.
 * @param seed Seed editor value; providers resolve blank or random-seed conventions before execution.
 * @param subSeed Variation seed editor value for providers supporting seed interpolation.
 * @param subSeedStrength Interpolation strength between the primary seed and variation seed.
 * @param selectedSampler Current provider sampler selection shown in the form.
 * @param selectedScheduler Current noise scheduling strategy shown in compatible provider controls.
 * @param availableForgeModules Forge modules advertised by the configured server.
 * @param selectedForgeModules Forge modules enabled for the next generation request.
 * @param availableSamplers Sampler identifiers advertised by the configured provider.
 * @param isLoadingSamplers Whether an AUTOMATIC1111 sampler request is running.
 * @param hasSamplerLoadFailed Whether the sampler field should offer an inline retry.
 * @param selectedStylePreset Stability AI style preset selected for the next request.
 * @param selectedClipGuidancePreset Stability AI CLIP guidance preset selected for the next request.
 * @param openAiCompatibleModel Arbitrary compatible-server image model identifier, independent of the built-in OpenAI catalog.
 * @param openAiOAuthAccountId Token-free account identity observed from protected session state.
 * @param openAiOAuthRevision Login revision captured when generation is submitted.
 * @param providerSelectionRevision Durable source-selection revision captured with submitted work.
 * @param openAiModel OpenAI image model selected for providers using the built-in OpenAI catalog.
 * @param openAiSize OpenAI output-size preset selected for compatible image models.
 * @param openAiQuality OpenAI output-quality preset selected for compatible image models.
 * @param falAiModel Fal AI endpoint defining the generation model and supported controls.
 * @param falAiImageSize Fal AI image-size preset sent to the selected endpoint.
 * @param falAiAcceleration Fal AI acceleration policy supported by the endpoint.
 * @param falAiSyncMode Whether Fal AI should return the image synchronously instead of using queued delivery.
 * @param mnnBackend Explicit platform-supported choice captured when MNN generation is submitted.
 * @param supportedMnnBackends Injected requested-backend vocabulary for the current platform.
 * @param mnnActivationId Exact validated MNN manifest SHA256 captured with the submitted model.
 * @param mnnModelId Catalog or custom model identity paired with the validated MNN artifact.
 * @param sdxlBackend Requested stable-diffusion.cpp execution backend, including automatic selection.
 * @param bonsaiBackend Requested Bonsai execution backend on supported platforms.
 * @param bonsaiBackendSelectionVisible Whether the current platform exposes the Bonsai backend selector.
 * @param arliAiModels Arli AI model identifiers available for selection.
 * @param arliAiModel Arli AI model identifier selected for image generation.
 * @param widthValidationError Inline width validation feedback, or null when the editor value is valid.
 * @param heightValidationError Inline height validation feedback, or null when the editor value is valid.
 * @param seedValidationError Seed feedback published through the injected validator.
 * @param subSeedValidationError Variation seed feedback for providers that use it.
 * @param nsfw Whether the selected provider permits adult-content generation.
 * @param batchCount Number of images requested, subject to provider batching capabilities.
 * @param hires AUTOMATIC1111 high-resolution second-pass configuration.
 * @param aDetailer AUTOMATIC1111 detail-refinement configuration; disabled for providers without this extension.
 * @param aDetailerAvailable Whether the configured server advertises ADetailer support.
 * @param aDetailerRefreshing Whether ADetailer extension availability is being refreshed.
 * @author Dmitriy Moroz
 */
@Immutable
data class TextToImageState(
    val loadingConfiguration: Boolean = true,
    val generationStartedAt: TimeMark? = null,
    val generating: Boolean = false,
    val savingImage: Boolean = false,
    val sharingImage: Boolean = false,
    val promptValidationError: UiText? = null,
    val error: UiText? = null,
    val message: UiText? = null,
    val screenModal: GenerationModal = GenerationModal.None,
    val results: List<AiGenerationResult> = emptyList(),
    val editTag: PromptTagEditRequest? = null,
    override val onBoardingDemo: Boolean = false,
    val platform: Platform = Platform.ANDROID,
    override val mode: ServerSource = ServerSource.AUTOMATIC1111,
    override val advancedToggleButtonVisible: Boolean = true,
    override val advancedOptionsVisible: Boolean = false,
    override val formPromptTaggedInput: Boolean = false,
    override val prompt: String = "",
    override val negativePrompt: String = "",
    override val width: String = DEFAULT_SIZE.toString(),
    override val height: String = DEFAULT_SIZE.toString(),
    override val samplingSteps: Int = 20,
    override val cfgScale: Float = 7f,
    override val restoreFaces: Boolean = false,
    override val seed: String = "",
    override val subSeed: String = "",
    override val subSeedStrength: Float = 0f,
    override val selectedSampler: String = "",
    override val selectedScheduler: Scheduler = Scheduler.AUTOMATIC,
    override val availableForgeModules: List<ForgeModule> = emptyList(),
    override val selectedForgeModules: List<ForgeModule> = emptyList(),
    override val availableSamplers: List<String> = emptyList(),
    override val isLoadingSamplers: Boolean = false,
    override val hasSamplerLoadFailed: Boolean = false,
    override val selectedStylePreset: StabilityAiStylePreset = StabilityAiStylePreset.NONE,
    override val selectedClipGuidancePreset: StabilityAiClipGuidance = StabilityAiClipGuidance.NONE,
    override val openAiCompatibleModel: String = "gpt-image-2",
    val openAiOAuthAccountId: String? = null,
    val openAiOAuthRevision: String? = null,
    val providerSelectionRevision: Long = 0L,
    override val openAiModel: OpenAiModel = OpenAiModel.default,
    override val openAiSize: OpenAiSize = OpenAiSize.W1024_H1024,
    override val openAiQuality: OpenAiQuality = OpenAiQuality.AUTO,
    override val falAiModel: FalAiModel = FalAiModel.defaultTextToImage,
    override val falAiImageSize: FalAiImageSize = FalAiImageSize.default,
    override val falAiAcceleration: FalAiAcceleration = FalAiAcceleration.default,
    override val falAiSyncMode: Boolean = false,
    override val mnnBackend: MnnBackend = MnnBackend.CPU,
    override val supportedMnnBackends: List<MnnBackend> = listOf(MnnBackend.CPU, MnnBackend.OPENCL),
    val mnnActivationId: String? = null,
    val mnnModelId: String? = null,
    override val sdxlBackend: SdxlBackend = SdxlBackend.AUTO,
    override val bonsaiBackend: BonsaiBackend = BonsaiBackend.AUTO,
    override val bonsaiBackendSelectionVisible: Boolean = false,
    override val arliAiModels: List<String> = emptyList(),
    override val arliAiModel: String = "",
    override val widthValidationError: UiText? = null,
    override val heightValidationError: UiText? = null,
    override val seedValidationError: UiText? = null,
    override val subSeedValidationError: UiText? = null,
    override val nsfw: Boolean = false,
    override val batchCount: Int = 1,
    override val hires: HiresConfig = HiresConfig.DISABLED,
    override val aDetailer: ADetailerConfig = ADetailerConfig.DISABLED,
    override val aDetailerAvailable: Boolean = false,
    override val aDetailerRefreshing: Boolean = false,
) : MviState, GenerationInputFormState {

    val localSourceSelected: Boolean
        get() = mode == ServerSource.LOCAL_MNN || mode == ServerSource.LOCAL_MICROSOFT_ONNX ||
                mode == ServerSource.LOCAL_GOOGLE_MEDIA_PIPE ||
                mode == ServerSource.LOCAL_STABLE_DIFFUSION_CPP ||
                mode == ServerSource.LOCAL_APPLE_CORE_ML ||
                mode == ServerSource.LOCAL_APPLE_BONSAI

    val hasValidationErrors: Boolean
        get() = promptValidationError != null ||
                widthValidationError != null ||
                heightValidationError != null ||
                seedValidationError != null ||
                subSeedValidationError != null ||
                error != null
}

/**
 * Converts the current txt2img state into the domain generation request.
 *
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.mapToPayload(): TextToImagePayload = TextToImagePayload(
    prompt = prompt.trim(),
    negativePrompt = negativePrompt.trim(),
    samplingSteps = samplingSteps,
    cfgScale = cfgScale,
    width = when (mode) {
        ServerSource.OPEN_AI -> openAiSize.width
        ServerSource.FAL_AI -> falAiImageSize.width
        ServerSource.LOCAL_APPLE_BONSAI -> width.toIntOrNull() ?: BONSAI_DEFAULT_SIZE
        else -> width.toIntOrNull() ?: DEFAULT_SIZE
    },
    height = when (mode) {
        ServerSource.OPEN_AI -> openAiSize.height
        ServerSource.FAL_AI -> falAiImageSize.height
        ServerSource.LOCAL_APPLE_BONSAI -> height.toIntOrNull() ?: BONSAI_DEFAULT_SIZE
        else -> height.toIntOrNull() ?: DEFAULT_SIZE
    },
    restoreFaces = restoreFaces,
    seed = seed.trim(),
    subSeed = subSeed.trim(),
    subSeedStrength = subSeedStrength,
    sampler = selectedSampler,
    scheduler = selectedScheduler.takeIf {
        mode == ServerSource.AUTOMATIC1111
    } ?: Scheduler.AUTOMATIC,
    nsfw = if (
        mode == ServerSource.HORDE ||
        mode == ServerSource.LOCAL_APPLE_CORE_ML ||
        mode == ServerSource.LOCAL_APPLE_BONSAI ||
        mode == ServerSource.FAL_AI
    ) nsfw else false,
    batchCount = if (
        mode == ServerSource.LOCAL_MICROSOFT_ONNX ||
        mode == ServerSource.LOCAL_STABLE_DIFFUSION_CPP ||
        mode == ServerSource.LOCAL_APPLE_BONSAI
    ) {
        1
    } else {
        batchCount
    },
    style = null,
    quality = openAiQuality.key.takeIf {
        mode == ServerSource.OPEN_AI && openAiQuality != OpenAiQuality.AUTO
    },
    openAiModel = openAiModel.takeIf { mode == ServerSource.OPEN_AI },
    stabilityAiClipGuidance = selectedClipGuidancePreset.takeIf { mode == ServerSource.STABILITY_AI },
    stabilityAiStylePreset = selectedStylePreset.takeIf { mode == ServerSource.STABILITY_AI },
    hires = hires.takeIf {
        mode == ServerSource.AUTOMATIC1111 && hires.enabled
    } ?: HiresConfig.DISABLED,
    aDetailer = aDetailer.takeIf {
        mode == ServerSource.AUTOMATIC1111 && aDetailer.enabled && aDetailerAvailable
    } ?: ADetailerConfig.DISABLED,
    forgeModules = selectedForgeModules.takeIf {
        mode == ServerSource.AUTOMATIC1111
    }.orEmpty(),
    falAiModel = falAiModel.takeIf {
        mode == ServerSource.FAL_AI
    } ?: FalAiModel.defaultTextToImage,
    falAiImageSize = falAiImageSize,
    falAiAcceleration = falAiAcceleration,
    sdxlBackend = sdxlBackend.takeIf {
        mode == ServerSource.LOCAL_STABLE_DIFFUSION_CPP
    } ?: SdxlBackend.AUTO,
    mnnBackend = mnnBackend,
    mnnActivationId = mnnActivationId.takeIf { mode == ServerSource.LOCAL_MNN },
    mnnModelId = mnnModelId.takeIf { mode == ServerSource.LOCAL_MNN },
    mnnProfile = "sd15-512-general-int8-v1".takeIf { mode == ServerSource.LOCAL_MNN },
    bonsaiBackend = bonsaiBackend.takeIf {
        mode == ServerSource.LOCAL_APPLE_BONSAI
    } ?: BonsaiBackend.AUTO,
    falAiSyncMode = falAiSyncMode,
    arliAiModel = arliAiModel.takeIf { mode == ServerSource.ARLI_AI }.orEmpty(),
    openAiCompatibleModel = openAiCompatibleModel.trim(),
    sourceSnapshot = mode,
    providerSelectionRevision = providerSelectionRevision,
    openAiOAuthAccountId = openAiOAuthAccountId.takeIf { mode == ServerSource.OPEN_AI_OAUTH },
    openAiOAuthRevision = openAiOAuthRevision.takeIf { mode == ServerSource.OPEN_AI_OAUTH },
)

internal const val DEFAULT_SIZE = 512
