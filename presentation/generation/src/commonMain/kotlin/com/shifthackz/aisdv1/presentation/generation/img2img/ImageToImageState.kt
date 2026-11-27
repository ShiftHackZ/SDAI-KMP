package com.shifthackz.aisdv1.presentation.generation.img2img

import kotlin.time.TimeMark
import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.FalAiAcceleration
import com.shifthackz.aisdv1.domain.entity.FalAiImageSize
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiQuality
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.generation.model.PromptTagEditRequest
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormState

/**
 * Render state for the retained image-to-image form and its inpaint editor.
 * Implements the shared generation-control contract without making every provider or control
 * applicable to img2img. [mapToPayload] selects request fields; shared-form compatibility values
 * such as OpenAI options, local backend selectors and hires do not enable unsupported sources.
 *
 * @param loadingConfiguration Whether the initial configured-source load has not completed.
 * @param imageBase64 Encoded source image used for generation and mask editing; blank means no input.
 * @param denoisingStrength Source-to-output transformation strength edited by the form, normally zero through one.
 * @param pickingImage Whether native input acquisition or random-image loading is in progress.
 * @param generationStartedAt Monotonic foreground start retained across dialog stages and composition recreation.
 * @param generating Whether a started generation or background-scheduling operation is active.
 * @param savingImage Whether the platform image-save operation is active.
 * @param sharingImage Whether output is being prepared for a platform share handoff; does not imply delivery.
 * @param inPaint Mask strokes and inpaint request controls, applied only when this form permits masks.
 * @param promptValidationError Prompt feedback from generation admission, or null when no prompt error is recorded.
 * @param error Inline operation error; a nonnull value also blocks the form-level canGenerate predicate.
 * @param message Inline status message from image-save or share preparation.
 * @param screenModal Active progress, result, error, benchmark or background-work modal.
 * @param results Transient form result list; completed generation dialogs carry their results in screenModal.
 * @param editTag Pending prompt-tag edit request, or null outside the tag editor.
 * @param onBoardingDemo Whether the form is being rendered as an onboarding demonstration.
 * @param platform Platform whose provider controls and local capabilities apply.
 * @param mode Selected provider whose visible controls and generation dispatch apply.
 * @param providerSelectionRevision Provider-selection revision copied into a submitted request to identify its captured configuration.
 * @param advancedToggleButtonVisible Whether the form exposes the advanced-control expansion action.
 * @param advancedOptionsVisible Whether advanced generation controls are currently expanded.
 * @param formPromptTaggedInput Whether prompts use the tag editor instead of plain text input.
 * @param upscaler Captured final-stage provider/model; null disables optional upscaling.
 * @param prompt Positive generation prompt, preserved verbatim.
 * @param negativePrompt Edited negative prompt; payload mapping trims its surrounding whitespace.
 * @param width Edited output width text; payload mapping uses a provider preset or a numeric fallback.
 * @param height Edited output height text; payload mapping uses a provider preset or a numeric fallback.
 * @param samplingSteps Requested inference step count.
 * @param cfgScale Requested classifier-free guidance scale.
 * @param restoreFaces Whether a supporting provider applies face restoration.
 * @param seed Edited seed text, including random sentinels accepted by the active provider validator.
 * @param subSeed Edited variation-seed text for providers that support variations.
 * @param subSeedStrength Variation seed blend strength for supporting providers.
 * @param selectedSampler Exact selected sampler identifier, reconciled with the active provider catalog.
 * @param selectedScheduler Selected AUTOMATIC1111 scheduler; automatic preserves normal server scheduling.
 * @param availableForgeModules Forge modules advertised by the active AUTOMATIC1111 endpoint.
 * @param selectedForgeModules Shared-form Forge selection reconciled with available choices; not forwarded by mapToPayload.
 * @param availableSamplers Sampler labels currently available for the active provider.
 * @param isLoadingSamplers Whether an AUTOMATIC1111 sampler request is running.
 * @param hasSamplerLoadFailed Whether the sampler field should offer an inline retry.
 * @param selectedStylePreset Selected Stability AI style preset, or NONE when disabled.
 * @param selectedClipGuidancePreset Selected Stability AI CLIP guidance preset, or NONE when disabled.
 * @param openAiModel Shared-form OpenAI model value; does not enable image-to-image for that source.
 * @param openAiSize Shared-form OpenAI size value; not forwarded by mapToPayload.
 * @param openAiQuality Shared-form OpenAI quality value; not forwarded by mapToPayload.
 * @param falAiModel Selected Fal.ai image-generation endpoint.
 * @param falAiImageSize Fal.ai output-size preset.
 * @param falAiAcceleration Fal.ai acceleration preset.
 * @param falAiSyncMode Whether a supporting Fal.ai endpoint requests synchronous output.
 * @param sdxlBackend Shared-form stable-diffusion.cpp backend value; not forwarded by this img2img mapper.
 * @param bonsaiBackend Shared-form Bonsai backend value; does not enable Bonsai image-to-image.
 * @param bonsaiBackendSelectionVisible Shared-form backend-control visibility flag initialized from the host platform.
 * @param arliAiModels Checkpoint identifiers advertised by the active ArliAI service.
 * @param arliAiModel Selected ArliAI checkpoint identifier.
 * @param widthValidationError Recorded width-validation feedback, or null when none is recorded.
 * @param heightValidationError Recorded height-validation feedback, or null when none is recorded.
 * @param seedValidationError Seed feedback published through the injected validator.
 * @param subSeedValidationError Variation seed feedback for providers that use it.
 * @param nsfw Safety preference forwarded only for Horde, Core ML and Fal.ai requests.
 * @param batchCount Number of requested images; providers may execute the batch sequentially.
 * @param hires Shared-form high-resolution configuration; not forwarded by mapToPayload.
 * @param aDetailer Extension configuration forwarded only for AUTOMATIC1111 when enabled and available.
 * @param aDetailerAvailable Whether the active AUTOMATIC1111 endpoint advertises ADetailer.
 * @param aDetailerRefreshing Whether ADetailer availability is being refreshed.
 * @author Dmitriy Moroz
 */
@Immutable
data class ImageToImageState(
    val upscaler: UpscalerConfiguration? = null,
    val loadingConfiguration: Boolean = true,
    val imageBase64: String = "",
    val denoisingStrength: Float = DEFAULT_DENOISING_STRENGTH,
    val pickingImage: Boolean = false,
    val generationStartedAt: TimeMark? = null,
    val generating: Boolean = false,
    val savingImage: Boolean = false,
    val sharingImage: Boolean = false,
    val inPaint: ImageInPaintState = ImageInPaintState(),
    val promptValidationError: String? = null,
    val error: String? = null,
    val message: String? = null,
    val screenModal: GenerationModal = GenerationModal.None,
    val results: List<AiGenerationResult> = emptyList(),
    val editTag: PromptTagEditRequest? = null,
    override val onBoardingDemo: Boolean = false,
    val platform: Platform = Platform.ANDROID,
    override val mode: ServerSource = ServerSource.AUTOMATIC1111,
    val providerSelectionRevision: Long = 0L,
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
    override val openAiModel: OpenAiModel = OpenAiModel.default,
    override val openAiSize: OpenAiSize = OpenAiSize.W1024_H1024,
    override val openAiQuality: OpenAiQuality = OpenAiQuality.AUTO,
    override val falAiModel: FalAiModel = FalAiModel.defaultImageToImage,
    override val falAiImageSize: FalAiImageSize = FalAiImageSize.default,
    override val falAiAcceleration: FalAiAcceleration = FalAiAcceleration.default,
    override val falAiSyncMode: Boolean = false,
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

    /**
     * Whether source-image text is present; this check does not decode or validate the image bytes.
     *
     * @author Dmitriy Moroz
     */
    val hasInputImage: Boolean
        get() = imageBase64.isNotBlank()

    /**
     * Whether the selected source belongs to the form's supported image-to-image provider set.
     *
     * @author Dmitriy Moroz
     */
    val sourceSupportsImageToImage: Boolean
        get() = mode == ServerSource.AUTOMATIC1111 ||
                mode == ServerSource.SWARM_UI ||
                mode == ServerSource.EASY_DIFFUSION ||
                mode == ServerSource.HORDE ||
                mode == ServerSource.HUGGING_FACE ||
                mode == ServerSource.STABILITY_AI ||
                mode == ServerSource.LOCAL_APPLE_CORE_ML ||
                mode == ServerSource.FAL_AI ||
                mode == ServerSource.ARLI_AI

    /**
     * Whether mask controls are allowed by this form, excluding Core ML and Easy Diffusion.
     * Image-to-image source availability is checked separately by [sourceSupportsImageToImage].
     *
     * @author Dmitriy Moroz
     */
    val sourceSupportsInPaint: Boolean
        get() = mode != ServerSource.LOCAL_APPLE_CORE_ML && mode != ServerSource.EASY_DIFFUSION

    /**
     * Whether prompt, dimension or seed feedback is present, or an inline operation error remains.
     *
     * @author Dmitriy Moroz
     */
    val hasValidationErrors: Boolean
        get() = promptValidationError != null ||
                widthValidationError != null ||
                heightValidationError != null ||
                seedValidationError != null ||
                subSeedValidationError != null ||
                error != null

    /**
     * Form-level submission eligibility: an input exists, its source is supported, no validation
     * or operation error remains, and neither image acquisition nor generation is active.
     * Background-work admission and request validation are performed separately by the action handler.
     *
     * @author Dmitriy Moroz
     */
    val canGenerate: Boolean
        get() = hasInputImage &&
                sourceSupportsImageToImage &&
                !hasValidationErrors &&
                !pickingImage &&
                !generating
}

/**
 * Captures the source/revision, image and normalized form values in an image-to-image request.
 * Trims prompt and seed text, uses Fal.ai preset dimensions or numeric dimensions with a 512-pixel
 * fallback, and restricts scheduler, safety and extension options to their applicable sources.
 * The mapper does not validate the request or encode mask strokes.
 *
 * @receiver Form snapshot admitted for generation.
 * @param maskBase64 Already encoded mask; null becomes an empty mask string.
 * @return Request carrying this snapshot's provider identity and supported generation parameters.
 * @author Dmitriy Moroz
 */
internal fun ImageToImageState.mapToPayload(
    maskBase64: String? = null,
): ImageToImagePayload = ImageToImagePayload(
    upscaler = upscaler,
    sourceSnapshot = mode,
    providerSelectionRevision = providerSelectionRevision,
    base64Image = imageBase64,
    base64MaskImage = maskBase64.orEmpty(),
    denoisingStrength = denoisingStrength,
    prompt = prompt.trim(),
    negativePrompt = negativePrompt.trim(),
    samplingSteps = samplingSteps,
    cfgScale = cfgScale,
    width = when (mode) {
        ServerSource.FAL_AI -> falAiImageSize.width
        else -> width.toIntOrNull() ?: DEFAULT_SIZE
    },
    height = when (mode) {
        ServerSource.FAL_AI -> falAiImageSize.height
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
        mode == ServerSource.FAL_AI
    ) nsfw else false,
    batchCount = batchCount,
    inPaintingMaskInvert = inPaint.maskMode.inverse,
    inPaintFullResPadding = inPaint.onlyMaskedPaddingPx,
    inPaintingFill = inPaint.maskContent.fill,
    inPaintFullRes = inPaint.area.fullRes,
    maskBlur = inPaint.maskBlur,
    stabilityAiClipGuidance = selectedClipGuidancePreset.takeIf { mode == ServerSource.STABILITY_AI },
    stabilityAiStylePreset = selectedStylePreset.takeIf { mode == ServerSource.STABILITY_AI },
    aDetailer = aDetailer.takeIf {
        mode == ServerSource.AUTOMATIC1111 && aDetailer.enabled && aDetailerAvailable
    } ?: ADetailerConfig.DISABLED,
    falAiModel = falAiModel.takeIf {
        mode == ServerSource.FAL_AI
    } ?: FalAiModel.defaultImageToImage,
    falAiImageSize = falAiImageSize,
    falAiAcceleration = falAiAcceleration,
    falAiSyncMode = falAiSyncMode,
    arliAiModel = arliAiModel.takeIf { mode == ServerSource.ARLI_AI }.orEmpty(),
)

internal const val DEFAULT_SIZE = 512
internal const val DEFAULT_DENOISING_STRENGTH = 0.75f
