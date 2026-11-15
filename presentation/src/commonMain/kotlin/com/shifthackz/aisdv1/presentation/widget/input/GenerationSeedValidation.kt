package com.shifthackz.aisdv1.presentation.widget.input

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.validation.seed.SeedValidator
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageState
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageState

/**
 * Recomputes seed feedback before publishing a changed text-to-image form.
 *
 * @param validator seed validation contract supplied by the composition root.
 * @param nativeValidator seed validator configured for native integer precision.
 * @author Dmitriy Moroz
 */
internal fun TextToImageState.withSeedValidation(
    validator: SeedValidator,
    nativeValidator: SeedValidator,
): TextToImageState = copy(
    seedValidationError = validateGenerationSeed(seed, mode, false, validator, nativeValidator),
    subSeedValidationError = validateGenerationSeed(subSeed, mode, true, validator, nativeValidator),
)

/**
 * Recomputes seed feedback before publishing a changed image-to-image form.
 *
 * @param validator seed validation contract supplied by the composition root.
 * @param nativeValidator seed validator configured for native integer precision.
 * @author Dmitriy Moroz
 */
internal fun ImageToImageState.withSeedValidation(
    validator: SeedValidator,
    nativeValidator: SeedValidator,
): ImageToImageState = copy(
    seedValidationError = validateGenerationSeed(seed, mode, false, validator, nativeValidator),
    subSeedValidationError = validateGenerationSeed(subSeed, mode, true, validator, nativeValidator),
)

/**
 * Maps seed validation to inline feedback for controls used by the active provider.
 * Web providers use the JavaScript safe integer range; other seed controls retain Long range.
 * Hidden seed controls cannot block generation after switching providers or applying history.
 *
 * @param input exact seed text retained by the generation form.
 * @param mode provider whose numeric constraints and visible controls apply.
 * @param isVariationSeed whether the input belongs to the optional variation-seed control.
 * @param validator injected validator configured for web numeric controls.
 * @param nativeValidator injected validator configured for native integer precision.
 * @return localized validation feedback, or null when valid or unused by the provider.
 * @author Dmitriy Moroz
 */
private fun validateGenerationSeed(
    input: String,
    mode: ServerSource,
    isVariationSeed: Boolean,
    validator: SeedValidator,
    nativeValidator: SeedValidator,
): UiText? {
    val isWebProvider = mode == ServerSource.AUTOMATIC1111 || mode == ServerSource.SWARM_UI
    if (isVariationSeed && !isWebProvider) return null
    if (mode == ServerSource.OPEN_AI || mode == ServerSource.OPEN_AI_COMPATIBLE || mode == ServerSource.OPEN_AI_OAUTH) {
        return null
    }
    val activeValidator = if (isWebProvider) validator else nativeValidator
    return when (val error = activeValidator(input).validationError) {
        SeedValidator.Error.InvalidFormat -> Localization.string("error_seed_format").asUiText()
        is SeedValidator.Error.TooLarge -> Localization.string("error_seed_max", error.maximum).asUiText()
        null -> null
    }
}
