package com.shifthackz.aisdv1.presentation.generation.widget.input

/**
 * Groups fields by their rendered control structure without changing provider-qualified identity.
 * Sampler retry feedback and extension notices stay separate from ordinary dropdowns and rows.
 *
 * @receiver Logical advanced field selected by the current provider's visibility rules.
 *
 * @return Reuse and prefetch-estimate category shared by structurally similar fields.
 *
 * @author Dmitriy Moroz
 */
internal fun GenerationFormField.contentType(): GenerationFormContentType = when (this) {
    GenerationFormField.SEED,
    GenerationFormField.VARIATION_SEED -> GenerationFormContentType.SEED_EDITOR

    GenerationFormField.VARIATION_STRENGTH,
    GenerationFormField.SAMPLING_STEPS,
    GenerationFormField.GUIDANCE_SCALE,
    GenerationFormField.BATCH,
    GenerationFormField.HIRES_SCALE,
    GenerationFormField.HIRES_STEPS,
    GenerationFormField.HIRES_DENOISING,
    GenerationFormField.ADETAILER_CONFIDENCE,
    GenerationFormField.ADETAILER_DENOISING -> GenerationFormContentType.SLIDER_EDITOR

    GenerationFormField.FAL_SYNC,
    GenerationFormField.NSFW,
    GenerationFormField.RESTORE_FACES,
    GenerationFormField.HIRES_ENABLED,
    GenerationFormField.ADETAILER_ENABLED -> GenerationFormContentType.SWITCH

    GenerationFormField.STYLE,
    GenerationFormField.CLIP_GUIDANCE,
    GenerationFormField.FAL_ACCELERATION,
    GenerationFormField.SCHEDULER,
    GenerationFormField.HIRES_UPSCALER,
    GenerationFormField.ADETAILER_MODEL -> GenerationFormContentType.DROPDOWN

    GenerationFormField.SAMPLER -> GenerationFormContentType.SAMPLER

    GenerationFormField.FORGE_MODULES -> GenerationFormContentType.MULTISELECT

    GenerationFormField.AFTER_SLIDERS -> GenerationFormContentType.SIMPLE_SLIDER

    GenerationFormField.ADETAILER_MISSING -> GenerationFormContentType.NOTICE_ACTIONS

}
