package com.shifthackz.aisdv1.presentation.generation.img2img

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionSubmissionFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTimeoutFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionHttpFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProtocolFailure

import com.shifthackz.aisdv1.presentation.generation.model.easyDiffusionSizeError

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.validation.ValidationResult
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ArliAiSampler
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.entity.StabilityAiSampler
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
 * Lower bound for image-to-image denoising strength.
 *
 * @author Dmitriy Moroz
 */
internal const val MIN_DENOISING_STRENGTH = 0f

/**
 * Upper bound for image-to-image denoising strength.
 *
 * @author Dmitriy Moroz
 */
internal const val MAX_DENOISING_STRENGTH = 1f

/**
 * Cache identity for AUTOMATIC1111 sampler discovery; demo endpoint changes invalidate the catalog.
 *
 * @param serverUrl AUTOMATIC1111 server root used for metadata and generation.
 * @param demoMode Whether AUTOMATIC1111 uses the configured demonstration endpoint.
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

/** Maps current provider progress to queued or sampling UI while preserving cancellation availability.
 * @param canCancelLocalGeneration Whether the active local engine supports cancellation in the current lifecycle.
 * @author Dmitriy Moroz */
internal fun ImageToImageState.progressModal(
    canCancelLocalGeneration: Boolean,
): GenerationModal = if (mode == ServerSource.EASY_DIFFUSION) {
    GenerationModal.Generating(
        title = Localization.string("srv_type_easy_diffusion").asUiText(),
        canCancel = true
    )
} else if (
    mode == ServerSource.LOCAL_MICROSOFT_ONNX ||
    mode == ServerSource.LOCAL_GOOGLE_MEDIA_PIPE ||
    mode == ServerSource.LOCAL_APPLE_CORE_ML
) {
    GenerationModal.Generating(
        title = if (mode == ServerSource.LOCAL_APPLE_CORE_ML) {
            Localization.string("communicating_core_ml_title").asUiText()
        } else {
            null
        },
        canCancel = canCancelLocalGeneration || (platform == Platform.ANDROID && mode in setOf(
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
        )),
    )
} else {
    GenerationModal.Communicating()
}

/**
 * Creates a single-image or batch result dialog; an empty result becomes a visible invalid-result error.
 *
 * @param autoSaveEnabled Whether successful results are already saved by the normal generation path.
 * @param reportEnabled Whether the result dialog offers reporting.
 *
 * @author Dmitriy Moroz
 */
internal fun List<AiGenerationResult>.toImageToImageResultModal(
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
internal fun ImageToImageState.validated(
    dimensionValidator: DimensionValidator,
): ImageToImageState {
    val widthResult = dimensionValidator(width)
    val heightResult = dimensionValidator(height)
    val sourceError = when {
        (mode == ServerSource.OPEN_AI_COMPATIBLE || mode == ServerSource.OPEN_AI_OAUTH) -> Localization.string(
            "open_ai_compatible_img2img_unsupported"
        )

        !sourceSupportsImageToImage && mode == ServerSource.OPEN_AI ->
            Localization.string("error_img2img_openai_unsupported")

        !sourceSupportsImageToImage ->
            Localization.string("error_img2img_local_android_only")

        mode == ServerSource.ARLI_AI && arliAiModel.isBlank() ->
            Localization.string("error_invalid")

        imageBase64.isBlank() ->
            Localization.string("error_img2img_select_input")

        else -> null
    }
    return copy(
        promptValidationError = null,
        widthValidationError = width.easyDiffusionSizeError()
            .takeIf { mode == ServerSource.EASY_DIFFUSION } ?: widthResult.errorText(),
        heightValidationError = height.easyDiffusionSizeError()
            .takeIf { mode == ServerSource.EASY_DIFFUSION } ?: heightResult.errorText(),
        error = sourceError,
    )
}

/**
 * Maps dimension-validation failures to concise user-facing text and returns null for valid input.
 *
 * @author Dmitriy Moroz
 */
internal fun ValidationResult<DimensionValidator.Error>.errorText(): UiText? {
    if (isValid) return null
    return UiText.Static(
        when (val error = validationError) {
            DimensionValidator.Error.Empty -> "Required"
            is DimensionValidator.Error.LessThanMinimum -> "Minimum ${error.min}px"
            is DimensionValidator.Error.BiggerThanMaximum -> "Maximum ${error.max}px"
            DimensionValidator.Error.Unexpected,
            null,
                -> "Invalid value"
        },
    )
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
internal fun ImageToImageState.withSettings(
    settings: Settings,
    stableDiffusionSamplers: List<String>?,
    forgeModules: List<ForgeModule>?,
    aDetailerAvailable: Boolean?,
    arliAiModels: List<String>?,
): ImageToImageState =
    copy(
        arliAiModel = settings.arliAiModel,
        providerSelectionRevision = settings.providerSelectionRevision
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
internal fun ImageToImageState.withSource(
    source: ServerSource,
    stableDiffusionSamplers: List<String>?,
    forgeModules: List<ForgeModule>?,
    aDetailerAvailable: Boolean?,
    arliAiModels: List<String>?,
): ImageToImageState {
    val samplers = when (source) {
        ServerSource.EASY_DIFFUSION -> listOf("ddim")
        ServerSource.STABILITY_AI -> StabilityAiSampler.entries.map { "$it" }
        ServerSource.ARLI_AI -> ArliAiSampler.supported
        else -> stableDiffusionSamplers.orEmpty()
    }
    val modules = forgeModules.orEmpty().takeIf {
        source == ServerSource.AUTOMATIC1111
    }.orEmpty()
    val arliModels = arliAiModels.orEmpty()
    return copy(
        mode = source,
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
    )
}

/**
 * Produces a safe user-facing generation error, including supported provider-specific failure categories.
 *
 * @author Dmitriy Moroz
 */
internal fun Throwable.localizedMessageText(): UiText {
    val message = message
    val text = when {
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
 * Returns a concise generation error string with a specific out-of-memory explanation.
 *
 * @author Dmitriy Moroz
 */
internal fun Throwable.localizedMessageString(): String {
    val message = message
    return when {
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
}
