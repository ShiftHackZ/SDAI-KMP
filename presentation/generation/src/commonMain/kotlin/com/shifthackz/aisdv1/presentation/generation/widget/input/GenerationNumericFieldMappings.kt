package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.core.common.math.roundTo
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.ADETAILER_CONFIDENCE_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.ADETAILER_CONFIDENCE_MIN
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.BATCH_RANGE_FAL_AI_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.BATCH_RANGE_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.BATCH_RANGE_MIN
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.CFG_SCALE_RANGE_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.CFG_SCALE_RANGE_MIN
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.DENOISING_STRENGTH_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.DENOISING_STRENGTH_MIN
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.HIRES_SCALE_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.HIRES_SCALE_MIN
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.HIRES_STEPS_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.HIRES_STEPS_MIN
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_RANGE_ARLI_AI_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_RANGE_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_RANGE_MIN
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.SAMPLING_STEPS_RANGE_STABILITY_AI_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.SUB_SEED_STRENGTH_MAX
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormConstants.SUB_SEED_STRENGTH_MIN
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Resolves only the display parameters of a numeric field before its shared editor is composed.
 * Provider bounds, clamping, tick counts and decimal precision match the existing controls.
 *
 * @param state Current numeric values and provider limits from the owning form.
 *
 * @receiver Logical field whose structural content type is SLIDER_EDITOR.
 *
 * @return Label and editor values without behavior or screen dependencies.
 *
 * @throws IllegalStateException If called for a field that is not a numeric editor.
 *
 * @author Dmitriy Moroz
 */
internal fun GenerationFormField.numericParameters(
    state: GenerationInputAdvancedOptionsState,
): GenerationNumericFieldParameters = when (this) {
    GenerationFormField.VARIATION_STRENGTH -> GenerationNumericFieldParameters(
        labelKey = "hint_sub_seed_strength",
        labelValue = "${state.subSeedStrength.roundTo(2)}",
        value = state.subSeedStrength,
        minimum = SUB_SEED_STRENGTH_MIN,
        maximum = SUB_SEED_STRENGTH_MAX,
        valueDiff = 0.01f,
    )

    GenerationFormField.SAMPLING_STEPS -> {
        val minimum = if (state.mode == ServerSource.FAL_AI) state.falAiModel.minInferenceSteps
        else SAMPLING_STEPS_RANGE_MIN
        val maximum = when (state.mode) {
            ServerSource.LOCAL_MNN,
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            ServerSource.LOCAL_APPLE_BONSAI -> SAMPLING_STEPS_LOCAL_DIFFUSION_MAX

            ServerSource.STABILITY_AI -> SAMPLING_STEPS_RANGE_STABILITY_AI_MAX
            ServerSource.FAL_AI -> state.falAiModel.maxInferenceSteps
            ServerSource.ARLI_AI -> SAMPLING_STEPS_RANGE_ARLI_AI_MAX
            else -> SAMPLING_STEPS_RANGE_MAX
        }
        val steps = state.samplingSteps.coerceIn(minimum, maximum)
        GenerationNumericFieldParameters(
            labelKey = "hint_sampling_steps",
            labelValue = "$steps",
            value = steps.toFloat(),
            minimum = minimum.toFloat(),
            maximum = maximum.toFloat(),
            fractionDigits = 0,
            steps = (abs(maximum - minimum) - 1).coerceAtLeast(0),
        )
    }

    GenerationFormField.GUIDANCE_SCALE -> {
        val minimum = if (state.mode == ServerSource.FAL_AI) state.falAiModel.minGuidanceScale
        else CFG_SCALE_RANGE_MIN.toFloat()
        val maximum = if (state.mode == ServerSource.FAL_AI) state.falAiModel.maxGuidanceScale
        else CFG_SCALE_RANGE_MAX.toFloat()
        val cfgScale = state.cfgScale.coerceIn(minimum, maximum)
        GenerationNumericFieldParameters(
            labelKey = "hint_cfg_scale",
            labelValue = "${cfgScale.roundTo(2)}",
            value = cfgScale,
            minimum = minimum,
            maximum = maximum,
            valueDiff = 0.5f,
            steps = ((abs(maximum - minimum) * 2).roundToInt() - 1).coerceAtLeast(0),
        )
    }

    GenerationFormField.BATCH -> {
        val maximum =
            if (state.mode == ServerSource.FAL_AI) BATCH_RANGE_FAL_AI_MAX else BATCH_RANGE_MAX
        val batchCount = state.batchCount.coerceIn(BATCH_RANGE_MIN, maximum)
        GenerationNumericFieldParameters(
            labelKey = "hint_batch",
            labelValue = "$batchCount",
            value = batchCount.toFloat(),
            minimum = BATCH_RANGE_MIN.toFloat(),
            maximum = maximum.toFloat(),
            fractionDigits = 0,
            steps = abs(BATCH_RANGE_MIN - maximum) - 1,
        )
    }

    GenerationFormField.HIRES_SCALE -> GenerationNumericFieldParameters(
        labelKey = "hint_hires_scale",
        labelValue = "${state.hires.scale.roundTo(1)}",
        value = state.hires.scale,
        minimum = HIRES_SCALE_MIN,
        maximum = HIRES_SCALE_MAX,
        valueDiff = 0.1f,
        fractionDigits = 1,
    )

    GenerationFormField.HIRES_STEPS -> GenerationNumericFieldParameters(
        labelKey = "hint_hires_steps",
        labelValue = "${state.hires.steps}",
        value = state.hires.steps.toFloat(),
        minimum = HIRES_STEPS_MIN.toFloat(),
        maximum = HIRES_STEPS_MAX.toFloat(),
        fractionDigits = 0,
        steps = abs(HIRES_STEPS_MAX - HIRES_STEPS_MIN) - 1,
    )

    GenerationFormField.HIRES_DENOISING -> GenerationNumericFieldParameters(
        labelKey = "hint_hires_denoising",
        labelValue = "${state.hires.denoisingStrength.roundTo(2)}",
        value = state.hires.denoisingStrength,
        minimum = DENOISING_STRENGTH_MIN,
        maximum = DENOISING_STRENGTH_MAX,
        valueDiff = 0.01f,
    )

    GenerationFormField.ADETAILER_CONFIDENCE -> GenerationNumericFieldParameters(
        labelKey = "hint_adetailer_confidence",
        labelValue = "${state.aDetailer.confidence.roundTo(2)}",
        value = state.aDetailer.confidence,
        minimum = ADETAILER_CONFIDENCE_MIN,
        maximum = ADETAILER_CONFIDENCE_MAX,
        valueDiff = 0.01f,
    )

    GenerationFormField.ADETAILER_DENOISING -> GenerationNumericFieldParameters(
        labelKey = "hint_adetailer_denoising",
        labelValue = "${state.aDetailer.denoisingStrength.roundTo(2)}",
        value = state.aDetailer.denoisingStrength,
        minimum = DENOISING_STRENGTH_MIN,
        maximum = DENOISING_STRENGTH_MAX,
        valueDiff = 0.01f,
    )

    else -> error("Field $this does not use a numeric editor")
}

/**
 * Maps one applied numeric edit back to the original form event. Configuration copies preserve
 * every unrelated Hires or ADetailer value supplied by the screen.
 *
 * @param value Clamped and rounded value emitted by the shared editor.
 * @param state Current configurations needed to preserve unrelated provider parameters.
 *
 * @receiver Logical field whose structural content type is SLIDER_EDITOR.
 *
 * @return The owning screen's existing numeric or configuration update event.
 *
 * @throws IllegalStateException If called for a field that is not a numeric editor.
 *
 * @author Dmitriy Moroz
 */
internal fun GenerationFormField.numericChangeEvent(
    value: Float,
    state: GenerationInputAdvancedOptionsState,
): GenerationInputFormEvent = when (this) {
    GenerationFormField.VARIATION_STRENGTH -> GenerationInputFormEvent.UpdateSubSeedStrength(value)
    GenerationFormField.SAMPLING_STEPS -> GenerationInputFormEvent.UpdateSamplingSteps(value.roundToInt())
    GenerationFormField.GUIDANCE_SCALE -> GenerationInputFormEvent.UpdateCfgScale(value)
    GenerationFormField.BATCH -> GenerationInputFormEvent.UpdateBatch(value.roundToInt())
    GenerationFormField.HIRES_SCALE -> GenerationInputFormEvent.UpdateHiresConfig(
        state.hires.copy(
            scale = value
        )
    )

    GenerationFormField.HIRES_STEPS -> GenerationInputFormEvent.UpdateHiresConfig(
        state.hires.copy(
            steps = value.roundToInt()
        )
    )

    GenerationFormField.HIRES_DENOISING -> GenerationInputFormEvent.UpdateHiresConfig(
        state.hires.copy(
            denoisingStrength = value
        )
    )

    GenerationFormField.ADETAILER_CONFIDENCE -> GenerationInputFormEvent.UpdateADetailerConfig(
        state.aDetailer.copy(
            confidence = value
        )
    )

    GenerationFormField.ADETAILER_DENOISING -> GenerationInputFormEvent.UpdateADetailerConfig(
        state.aDetailer.copy(
            denoisingStrength = value
        )
    )

    else -> error("Field $this does not use a numeric editor")
}
