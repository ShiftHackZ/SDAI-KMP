package com.shifthackz.aisdv1.presentation.widget.input

/**
 * Stable identities for independently composed advanced fields. The provider name and field name
 * form each lazy item's saveable key, so inserting another control does not replace its state.
 *
 * @author Dmitriy Moroz
 */
internal enum class GenerationFormField {
    SAMPLER,
    STYLE,
    CLIP_GUIDANCE,
    SEED,
    FAL_ACCELERATION,
    FAL_SYNC,
    NSFW,
    VARIATION_SEED,
    VARIATION_STRENGTH,
    SAMPLING_STEPS,
    GUIDANCE_SCALE,
    AFTER_SLIDERS,
    BATCH,
    RESTORE_FACES,
    SCHEDULER,
    FORGE_MODULES,
    HIRES_ENABLED,
    HIRES_UPSCALER,
    HIRES_SCALE,
    HIRES_STEPS,
    HIRES_DENOISING,
    ADETAILER_ENABLED,
    ADETAILER_MISSING,
    ADETAILER_MODEL,
    ADETAILER_CONFIDENCE,
    ADETAILER_DENOISING,
}
