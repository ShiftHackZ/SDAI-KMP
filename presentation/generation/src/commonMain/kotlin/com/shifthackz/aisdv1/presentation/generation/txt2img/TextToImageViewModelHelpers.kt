package com.shifthackz.aisdv1.presentation.generation.txt2img

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionSubmissionFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTimeoutFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionHttpFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProtocolFailure

import com.shifthackz.aisdv1.presentation.generation.model.easyDiffusionSizeError

import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleHttpFailure
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleImageFailure
import com.shifthackz.aisdv1.presentation.components.model.localizedDescription
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.validation.ValidationResult
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ArliAiSampler
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationFailure
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.presentation.components.widget.work.toUserMessage
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.entity.StabilityAiSampler
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal

/**
 * Minimum inference step count accepted by the form.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_STEPS = 1

/**
 * Maximum inference step count accepted by the form.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_STEPS = 150

/**
 * Smallest supported image batch.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_BATCH_COUNT = 1

/**
 * Largest image batch selectable in the form.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_BATCH_COUNT = 20

/**
 * Minimum selectable classifier-free guidance scale.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_CFG_SCALE = 1f

/**
 * Maximum selectable classifier-free guidance scale.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_CFG_SCALE = 35f

/**
 * Initial Bonsai pixel size selected when switching from another provider.
 *
 * @author Dmitriy Moroz
 */
internal const val BONSAI_DEFAULT_SIZE = 128

/**
 * Required Bonsai pixel alignment used by form validation.
 *
 * @author Dmitriy Moroz
 */
internal const val BONSAI_SIZE_MULTIPLE = 32

/**
 * Lower bound for variation-seed blending.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_SUB_SEED_STRENGTH = 0f

/**
 * Upper bound for variation-seed blending.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_SUB_SEED_STRENGTH = 1f

/**
 * Cache identity for AUTOMATIC1111 sampler discovery; demo endpoint changes invalidate the catalog.
 *
 * @param serverUrl AUTOMATIC1111 server root used for metadata and generation.
 * @param demoMode Whether AUTOMATIC1111 uses the configured demonstration endpoint.
 *
 * @author Dmitriy Moroz
 */
internal data class StableDiffusionSamplersKey(
    /**
     * AUTOMATIC1111 server root used for metadata and generation.
     *
     * @author Dmitriy Moroz
     */
    val serverUrl: String,
    /**
     * Whether AUTOMATIC1111 uses the configured demonstration endpoint.
     *
     * @author Dmitriy Moroz
     */
    val demoMode: Boolean,
)

/**
 * Maps provider progress to queued, sampling or overall-percentage UI while preserving cancellation.
 *
 * @param canCancelLocalGeneration Whether the active local engine supports cancellation in the current lifecycle.
 *
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.progressModal(
    canCancelLocalGeneration: Boolean,
): GenerationModal = when {
    mode == ServerSource.EASY_DIFFUSION -> GenerationModal.Generating(
        title = Localization.string("srv_type_easy_diffusion").asUiText(), canCancel = true,
    )

    mode == ServerSource.LOCAL_APPLE_CORE_ML -> GenerationModal.Generating(
        title = Localization.string("communicating_core_ml_title").asUiText(),
        canCancel = canCancelLocalGeneration,
    )

    mode == ServerSource.LOCAL_APPLE_BONSAI -> GenerationModal.Generating(
        title = "Bonsai Image".asUiText(),
        canCancel = canCancelLocalGeneration || platform == Platform.ANDROID,
        nativeEngine = NativeGenerationEngine.BONSAI.takeIf { platform == Platform.ANDROID },
        nativeStage = NativeGenerationStage.PREPARING.takeIf { platform == Platform.ANDROID },
    )

    mode == ServerSource.LOCAL_MNN && platform == Platform.IOS -> GenerationModal.Generating(
        title = "MNN".asUiText(), canCancel = true, isOverallProgress = true,
    )

    localSourceSelected -> GenerationModal.Generating(
        canCancel = canCancelLocalGeneration || (platform == Platform.ANDROID && mode in setOf(
            ServerSource.LOCAL_MNN,
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
        )),
        nativeEngine = when (mode) {
            ServerSource.LOCAL_MNN -> NativeGenerationEngine.MNN
            ServerSource.LOCAL_MICROSOFT_ONNX -> NativeGenerationEngine.ONNX
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> NativeGenerationEngine.SDCPP
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> NativeGenerationEngine.MEDIA_PIPE
            else -> null
        },
        nativeStage = NativeGenerationStage.PREPARING,
    )

    else -> GenerationModal.Communicating()
}

/**
 * Creates a single-image or batch result dialog; an empty result becomes a visible invalid-result error.
 *
 * @param autoSaveEnabled Whether successful results are already saved by the normal generation path.
 * @param reportEnabled Whether the result dialog offers reporting.
 *
 * @author Dmitriy Moroz
 */
internal fun List<AiGenerationResult>.toTextToImageResultModal(
    autoSaveEnabled: Boolean,
    reportEnabled: Boolean,
): GenerationModal =
    takeIf(List<AiGenerationResult>::isNotEmpty)
        ?.let {
            GenerationModal.Image.create(
                list = it,
                autoSaveEnabled = autoSaveEnabled,
                reportEnabled = reportEnabled,
            )
        }
        ?: GenerationModal.Error(Localization.string("error_invalid").asUiText())

/**
 * Builds visible dimension and provider validation errors before generation; Easy Diffusion additionally requires multiples of eight.
 *
 * @param dimensionValidator Injected validator defining supported pixel-size bounds.
 *
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.validated(
    dimensionValidator: DimensionValidator,
): TextToImageState {
    val compatible = mode == ServerSource.OPEN_AI_COMPATIBLE || mode == ServerSource.OPEN_AI_OAUTH
    val validateDimensions = mode != ServerSource.OPEN_AI && !compatible
    val widthResult = dimensionValidator(width).takeIf { validateDimensions }
    val heightResult = dimensionValidator(height).takeIf { validateDimensions }
    val sourceError = when {
        mode == ServerSource.OPEN_AI_COMPATIBLE && openAiCompatibleModel.isBlank() -> Localization.string(
            "open_ai_compatible_model_required"
        ).asUiText()

        mode == ServerSource.ARLI_AI && arliAiModel.isBlank() -> Localization.string("error_invalid")
            .asUiText()

        else -> null
    }
    val validateBonsaiDimensions = mode == ServerSource.LOCAL_APPLE_BONSAI
    return copy(
        promptValidationError = null,
        widthValidationError = width.easyDiffusionSizeError()
            .takeIf { mode == ServerSource.EASY_DIFFUSION }
            ?: widthResult?.errorMessage()
            ?: width.bonsaiSizeError().takeIf { validateBonsaiDimensions }
            ?: Localization.string("open_ai_compatible_positive_size").asUiText()
                .takeIf { compatible && (width.toIntOrNull() ?: 0) <= 0 },
        heightValidationError = height.easyDiffusionSizeError()
            .takeIf { mode == ServerSource.EASY_DIFFUSION }
            ?: heightResult?.errorMessage()
            ?: height.bonsaiSizeError().takeIf { validateBonsaiDimensions }
            ?: Localization.string("open_ai_compatible_positive_size").asUiText()
                .takeIf { compatible && (height.toIntOrNull() ?: 0) <= 0 },
        error = sourceError,
    )
}

/**
 * Allows generation only after prompt, dimensions, seeds and provider-specific requirements have no validation errors.
 *
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.canGenerate(): Boolean =
    !hasValidationErrors

/**
 * Maps typed dimension-validation failures to localized text and returns null for valid input.
 *
 * @author Dmitriy Moroz
 */
internal fun ValidationResult<DimensionValidator.Error>.errorMessage(): UiText? {
    if (isValid) return null
    return UiText.Static(
        when (val error = validationError) {
            DimensionValidator.Error.Empty -> Localization.string("error_empty")
            is DimensionValidator.Error.LessThanMinimum -> Localization.string(
                "error_min_size",
                error.min
            )

            is DimensionValidator.Error.BiggerThanMaximum -> Localization.string(
                "error_max_size",
                error.max
            )

            DimensionValidator.Error.Unexpected,
            null,
                -> Localization.string("error_invalid")
        },
    )
}

/**
 * Returns localized Bonsai size feedback when this dimension violates the supported integer alignment.
 *
 * @author Dmitriy Moroz
 */

private fun String.bonsaiSizeError(): UiText? {
    val size = toIntOrNull() ?: return null
    if (size % BONSAI_SIZE_MULTIPLE == 0) return null
    return UiText.Static(Localization.string("error_size_multiple", BONSAI_SIZE_MULTIPLE))
}

/**
 * Applies current provider selection and form preferences while retaining user-entered generation controls.
 *
 * @param settings Current persisted provider selection and form preferences.
 * @param stableDiffusionSamplers Latest AUTOMATIC1111 sampler catalog, or null when unavailable.
 * @param forgeModules Available AUTOMATIC1111 Forge module identifiers, or null when unavailable.
 * @param aDetailerAvailable Whether the selected AUTOMATIC1111 endpoint advertises ADetailer.
 * @param arliAiModels Available ArliAI checkpoint identifiers.
 *
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.withSettings(
    settings: Settings,
    stableDiffusionSamplers: List<String>?,
    forgeModules: List<ForgeModule>?,
    aDetailerAvailable: Boolean?,
    arliAiModels: List<String>?,
): TextToImageState =
    copy(
        arliAiModel = settings.arliAiModel,
        providerSelectionRevision = settings.providerSelectionRevision,
        screenModal = if (mode == ServerSource.OPEN_AI_OAUTH && providerSelectionRevision != settings.providerSelectionRevision) GenerationModal.None else screenModal,
        results = if (mode == ServerSource.OPEN_AI_OAUTH && providerSelectionRevision != settings.providerSelectionRevision) emptyList() else results,
    )
        .withSource(
            settings.source,
            stableDiffusionSamplers,
            forgeModules,
            aDetailerAvailable,
            arliAiModels
        )
        .copy(
            advancedToggleButtonVisible = !settings.formAdvancedOptionsAlwaysShow,
            advancedOptionsVisible = if (settings.formAdvancedOptionsAlwaysShow) {
                true
            } else {
                advancedOptionsVisible
            },
            formPromptTaggedInput = settings.formPromptTaggedInput,
        )

/**
 * Reconciles selected samplers and provider-specific modules with the newly selected provider catalog.
 *
 * @param source Provider that the form should render and dispatch to.
 * @param stableDiffusionSamplers Latest AUTOMATIC1111 sampler catalog, or null when unavailable.
 * @param forgeModules Available AUTOMATIC1111 Forge modules, or null when unavailable.
 * @param aDetailerAvailable Whether the selected AUTOMATIC1111 endpoint advertises ADetailer.
 * @param arliAiModels Available ArliAI checkpoint identifiers.
 *
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.withSource(
    source: ServerSource,
    stableDiffusionSamplers: List<String>?,
    forgeModules: List<ForgeModule>?,
    aDetailerAvailable: Boolean?,
    arliAiModels: List<String>?,
): TextToImageState {
    val samplers = when (source) {
        ServerSource.LOCAL_MNN -> listOf("PNDM")
        ServerSource.EASY_DIFFUSION -> listOf("ddim")
        ServerSource.STABILITY_AI -> StabilityAiSampler.entries.map { "$it" }
        ServerSource.ARLI_AI -> ArliAiSampler.supported
        else -> stableDiffusionSamplers.orEmpty()
    }
    val modules = forgeModules.orEmpty().takeIf {
        source == ServerSource.AUTOMATIC1111
    }.orEmpty()
    val arliModels = arliAiModels.orEmpty()
    val switchingToBonsai = source == ServerSource.LOCAL_APPLE_BONSAI &&
            mode != ServerSource.LOCAL_APPLE_BONSAI
    return copy(
        mode = source,
        negativePrompt = if (source == ServerSource.LOCAL_MNN) "" else negativePrompt,
        cfgScale = if (source == ServerSource.LOCAL_MNN) 7.5f else cfgScale,
        samplingSteps = if (source == ServerSource.LOCAL_MNN) samplingSteps.coerceIn(
            1,
            50
        ) else samplingSteps,
        batchCount = if (source == ServerSource.LOCAL_MNN) 1 else batchCount,
        availableSamplers = samplers,
        selectedSampler = selectedSampler.takeIf { sampler -> samplers.contains(sampler) }
            ?: samplers.firstOrNull()
            ?: selectedSampler,
        availableForgeModules = modules,
        selectedForgeModules = selectedForgeModules.filter(modules::contains),
        arliAiModels = arliModels.takeIf { source == ServerSource.ARLI_AI }.orEmpty(),
        arliAiModel = if (source == ServerSource.ARLI_AI) {
            arliAiModel.takeIf(arliModels::contains)
                ?: arliModels.firstOrNull()
                ?: arliAiModel
        } else {
            arliAiModel
        },
        aDetailerAvailable = if (source == ServerSource.AUTOMATIC1111) {
            aDetailerAvailable ?: this.aDetailerAvailable
        } else {
            true
        },
        aDetailerRefreshing = if (source == ServerSource.AUTOMATIC1111) {
            aDetailerRefreshing
        } else {
            false
        },
        width = if (source == ServerSource.LOCAL_MNN) "512" else if (switchingToBonsai) {
            BONSAI_DEFAULT_SIZE.toString()
        } else {
            width
        },
        height = if (source == ServerSource.LOCAL_MNN) "512" else if (switchingToBonsai) {
            BONSAI_DEFAULT_SIZE.toString()
        } else {
            height
        },
    )
}

/**
 * Converts provider and controlled native failures into localized recovery text.
 * Native failures use the captured engine/backend without displaying native exception contents.
 *
 * @return Message suitable for a generation error modal.
 *
 * @author Dmitriy Moroz
 */
internal fun Throwable.localizedMessageText(): UiText {
    val message = message
    val text = when {
        this is NativeGenerationFailure -> toUserMessage()
        this is OpenAiOAuthFailure -> localizedDescription()
        this is OpenAiCompatibleHttpFailure -> localizedDescription()
        this is OpenAiCompatibleImageFailure -> Localization.string("open_ai_compatible_image_failure")
        this is EasyDiffusionSubmissionFailure -> Localization.string("easy_diffusion_submission_unknown")
        this is EasyDiffusionTimeoutFailure -> Localization.string("easy_diffusion_timeout")
        this is EasyDiffusionHttpFailure || this is EasyDiffusionProtocolFailure -> Localization.string(
            "easy_diffusion_failure"
        )

        message == null -> Localization.string("error_invalid")
        message.contains("out of memory", ignoreCase = true) ||
                message.contains("OutOfMemoryError", ignoreCase = true) ->
            Localization.string("error_generation_out_of_memory")

        else -> message
    }
    return UiText.Static(text)
}

/**
 * Reconciles restored or stale form inputs with the single supported MNN profile before rendering.
 * Values are transient form state; MNN does not overwrite other providers' saved diffusion defaults.
 *
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.withMnnProfile(): TextToImageState =
    if (mode == ServerSource.LOCAL_MNN) {
        copy(
            width = "512",
            height = "512",
            cfgScale = 7.5f,
            samplingSteps = samplingSteps.coerceIn(1, 50),
            negativePrompt = "",
            selectedSampler = "PNDM",
            batchCount = 1,
            restoreFaces = false
        )
    } else this
