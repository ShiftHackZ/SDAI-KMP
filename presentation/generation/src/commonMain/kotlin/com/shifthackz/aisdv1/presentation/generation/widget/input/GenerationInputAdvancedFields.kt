package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Selects logical advanced rows without composing any of their controls. Offscreen rows are
 * emitted as lazy items rather than children of one eager generation-form item.
 *
 * @param state Current provider capabilities and expanded option selections.
 * @param isImg2Img Whether text-only Forge and Hires controls are unavailable.
 *
 * @return Ordered fields for the selected provider; empty when advanced options are hidden.
 *
 * @author Dmitriy Moroz
 */
internal fun generationInputAdvancedFields(
    state: GenerationInputAdvancedOptionsState,
    isImg2Img: Boolean,
): List<GenerationFormField> = buildList {
    val mode = state.mode
    if (!state.isVisible || mode == ServerSource.OPEN_AI ||
        mode == ServerSource.OPEN_AI_COMPATIBLE || mode == ServerSource.OPEN_AI_OAUTH
    ) return@buildList
    if (mode == ServerSource.STABILITY_AI || mode == ServerSource.ARLI_AI ||
        mode == ServerSource.AUTOMATIC1111
    ) add(GenerationFormField.SAMPLER)
    if (mode == ServerSource.STABILITY_AI) {
        add(GenerationFormField.STYLE)
        add(GenerationFormField.CLIP_GUIDANCE)
    }
    add(GenerationFormField.SEED)
    if (mode == ServerSource.FAL_AI) {
        add(GenerationFormField.FAL_ACCELERATION)
        add(GenerationFormField.FAL_SYNC)
    }
    if (mode == ServerSource.HORDE || mode == ServerSource.FAL_AI ||
        mode == ServerSource.LOCAL_APPLE_CORE_ML
    ) add(GenerationFormField.NSFW)
    if (mode == ServerSource.AUTOMATIC1111 || mode == ServerSource.SWARM_UI) {
        add(GenerationFormField.VARIATION_SEED)
    }
    if (mode == ServerSource.AUTOMATIC1111 || mode == ServerSource.SWARM_UI ||
        mode == ServerSource.HORDE
    ) add(GenerationFormField.VARIATION_STRENGTH)
    add(GenerationFormField.SAMPLING_STEPS)
    if (mode != ServerSource.LOCAL_MNN && mode != ServerSource.LOCAL_GOOGLE_MEDIA_PIPE) add(
        GenerationFormField.GUIDANCE_SCALE
    )
    if (mode == ServerSource.AUTOMATIC1111 || mode == ServerSource.SWARM_UI ||
        mode == ServerSource.STABILITY_AI || mode == ServerSource.ARLI_AI ||
        mode == ServerSource.HORDE || mode == ServerSource.LOCAL_APPLE_CORE_ML ||
        mode == ServerSource.LOCAL_APPLE_BONSAI || mode == ServerSource.EASY_DIFFUSION
    ) add(GenerationFormField.AFTER_SLIDERS)
    if (mode == ServerSource.AUTOMATIC1111) {
        add(GenerationFormField.SCHEDULER)
        if (!isImg2Img && state.availableForgeModules.isNotEmpty()) {
            add(GenerationFormField.FORGE_MODULES)
        }
        if (!isImg2Img) {
            add(GenerationFormField.HIRES_ENABLED)
            if (state.hires.enabled) {
                add(GenerationFormField.HIRES_UPSCALER)
                add(GenerationFormField.HIRES_SCALE)
                add(GenerationFormField.HIRES_STEPS)
                add(GenerationFormField.HIRES_DENOISING)
            }
        }
        add(GenerationFormField.ADETAILER_ENABLED)
        if (state.aDetailer.enabled) {
            if (state.isADetailerAvailable) {
                add(GenerationFormField.ADETAILER_MODEL)
                add(GenerationFormField.ADETAILER_CONFIDENCE)
                add(GenerationFormField.ADETAILER_DENOISING)
            } else add(GenerationFormField.ADETAILER_MISSING)
        }
    }
    if (mode != ServerSource.LOCAL_MNN && mode != ServerSource.LOCAL_GOOGLE_MEDIA_PIPE &&
        mode != ServerSource.LOCAL_MICROSOFT_ONNX &&
        mode != ServerSource.LOCAL_STABLE_DIFFUSION_CPP &&
        mode != ServerSource.LOCAL_APPLE_BONSAI
    ) add(GenerationFormField.BATCH)
    if (mode == ServerSource.AUTOMATIC1111) add(GenerationFormField.RESTORE_FACES)
}
