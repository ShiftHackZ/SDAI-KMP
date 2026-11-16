package com.shifthackz.aisdv1.presentation.screen.img2img

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
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
import com.shifthackz.aisdv1.presentation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.model.PromptTagEditRequest
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormState

/**
 * Complete render state for img2img.
 *
 * @param isLoadingSamplers Whether an AUTOMATIC1111 sampler request is running.
 * @param hasSamplerLoadFailed Whether the sampler field should offer an inline retry.
 * @param seedValidationError Seed feedback published through the injected validator.
 * @param subSeedValidationError Variation seed feedback for providers that use it.
 *
 * It extends the shared generation form state with source-image, inpaint, and
 * result handling fields while keeping platform-aware local provider controls
 * consistent with the txt2img screen.
 *
 * @param providerSelectionRevision Durable source revision captured with queued image requests.
 * @param loadingConfiguration Whether saved provider controls are still being loaded.
 * @param imageBase64 Original encoded source image; empty until an input image is chosen.
 * @param denoisingStrength Image-to-image denoising strength in the range zero through one.
 * @param pickingImage Whether the platform picker or random input download is active.
 * @param generating Whether this form has dispatched generation and awaits completion.
 * @param savingImage Whether media-store export is in progress.
 * @param sharingImage Whether platform sharing is being prepared.
 * @param inPaint Current user mask strokes and paint controls; only supported providers consume them.
 * @param promptValidationError Blocking prompt feedback, or null when the visible prompt is valid.
 * @param error Current user-facing operation error, or null when no error is shown.
 * @param message Current nonblocking user-facing completion message.
 * @param screenModal Active progress, result, benchmark or background-work dialog.
 * @param results Completed results retained by the current form/result workflow.
 * @param editTag Pending prompt-tag edit request, or null outside the tag editor.
 * @param onBoardingDemo Whether this form is a nonpersistent onboarding demonstration.
 * @param platform Platform whose provider controls and local capabilities apply.
 * @param mode Selected provider whose visible controls and generation dispatch apply.
 * @param advancedToggleButtonVisible Whether the form exposes the advanced-control expansion action.
 * @param advancedOptionsVisible Whether advanced generation controls are currently expanded.
 * @param formPromptTaggedInput Whether prompts use the tag editor instead of plain text input.
 * @param prompt Positive generation prompt, preserved verbatim.
 * @param negativePrompt Negative generation prompt; unsupported providers omit it.
 * @param width Requested output width in pixels.
 * @param height Requested output height in pixels.
 * @param samplingSteps Requested inference step count.
 * @param cfgScale Requested classifier-free guidance scale.
 * @param restoreFaces Whether a supporting provider applies face restoration.
 * @param seed Decimal seed or the normal blank/-1 random sentinel.
 * @param subSeed Variation seed for supporting providers only.
 * @param subSeedStrength Variation seed blend strength for supporting providers.
 * @param selectedSampler Exact selected sampler identifier, reconciled with the active provider catalog.
 * @param selectedScheduler Selected AUTOMATIC1111 scheduler; automatic preserves normal server scheduling.
 * @param availableForgeModules Forge modules advertised by the active AUTOMATIC1111 endpoint.
 * @param selectedForgeModules Forge modules included in requests for supporting endpoints.
 * @param availableSamplers Sampler labels currently available for the active provider.
 * @param selectedStylePreset Selected Stability AI style preset, or NONE when disabled.
 * @param selectedClipGuidancePreset Selected Stability AI CLIP guidance preset, or NONE when disabled.
 * @param openAiModel Official OpenAI image-model selection.
 * @param openAiSize Official OpenAI image-size preset.
 * @param openAiQuality Official OpenAI output-quality selection.
 * @param falAiModel Selected Fal.ai image-generation endpoint.
 * @param falAiImageSize Fal.ai output-size preset.
 * @param falAiAcceleration Fal.ai acceleration preset.
 * @param falAiSyncMode Whether a supporting Fal.ai endpoint requests synchronous output.
 * @param sdxlBackend Selected stable-diffusion.cpp execution backend.
 * @param bonsaiBackend Selected Bonsai execution backend.
 * @param bonsaiBackendSelectionVisible Whether the current platform exposes Bonsai backend selection.
 * @param arliAiModels Checkpoint identifiers advertised by the active ArliAI service.
 * @param arliAiModel Selected ArliAI checkpoint identifier.
 * @param widthValidationError Localized width feedback, or null when the dimension is valid.
 * @param heightValidationError Localized height feedback, or null when the dimension is valid.
 * @param nsfw Provider-specific safety-filter preference.
 * @param batchCount Number of requested images; providers may execute the batch sequentially.
 * @param hires AUTOMATIC1111 high-resolution second-pass controls.
 * @param aDetailer AUTOMATIC1111 ADetailer extension configuration.
 * @param aDetailerAvailable Whether the active AUTOMATIC1111 endpoint advertises ADetailer.
 * @param aDetailerRefreshing Whether ADetailer availability is being refreshed.
 * @author Dmitriy Moroz
 */
@Immutable
data class ImageToImageState(
    val loadingConfiguration: Boolean = true,
    val imageBase64: String = "",
    val denoisingStrength: Float = DEFAULT_DENOISING_STRENGTH,
    val pickingImage: Boolean = false,
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

    val hasInputImage: Boolean
        get() = imageBase64.isNotBlank()

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

    val sourceSupportsInPaint: Boolean
        get() = mode != ServerSource.LOCAL_APPLE_CORE_ML && mode != ServerSource.EASY_DIFFUSION

    val hasValidationErrors: Boolean
        get() = promptValidationError != null ||
                widthValidationError != null ||
                heightValidationError != null ||
                seedValidationError != null ||
                subSeedValidationError != null ||
                error != null

    val canGenerate: Boolean
        get() = hasInputImage &&
                sourceSupportsImageToImage &&
                !hasValidationErrors &&
                !pickingImage &&
                !generating
}

/**
 * Converts the current img2img state into the domain generation request.
 * @param maskBase64 Optional encoded inpainting mask; null preserves the current empty-mask default.
 * @author Dmitriy Moroz
 */
internal fun ImageToImageState.mapToPayload(
    maskBase64: String? = null,
): ImageToImagePayload = ImageToImagePayload(
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
