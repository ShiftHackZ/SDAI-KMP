package com.shifthackz.aisdv1.presentation.widget.input

import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Selects basic controls in their established provider-specific order without composing them.
 * The public Column wrapper and lazy generation screens use the same selection rules.
 *
 * @param state Current provider capabilities and basic field visibility.
 * @return Ordered logical controls, excluding headers, advanced fields and status content.
 * @author Dmitriy Moroz
 */
internal fun generationInputBaseFields(
    state: GenerationInputFormState,
): List<GenerationBaseFormField> = buildList {
    val mode = state.mode
    if (!state.onBoardingDemo) {
        when (mode) {
            ServerSource.AUTOMATIC1111,
            ServerSource.SWARM_UI,
            ServerSource.EASY_DIFFUSION,
            ServerSource.STABILITY_AI,
            ServerSource.HUGGING_FACE,
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            ServerSource.LOCAL_APPLE_CORE_ML,
            ServerSource.LOCAL_APPLE_BONSAI,
            ServerSource.OPEN_AI,
            ServerSource.FAL_AI,
            ServerSource.ARLI_AI -> add(GenerationBaseFormField.PROVIDER)

            else -> Unit
        }
        if (mode == ServerSource.LOCAL_STABLE_DIFFUSION_CPP) add(GenerationBaseFormField.SDXL_BACKEND)
        if (mode == ServerSource.LOCAL_APPLE_BONSAI && state.bonsaiBackendSelectionVisible) {
            add(GenerationBaseFormField.BONSAI_BACKEND)
        }
    }
    val isCompatibleProvider =
        mode == ServerSource.OPEN_AI_COMPATIBLE || mode == ServerSource.OPEN_AI_OAUTH
    if (isCompatibleProvider) {
        add(GenerationBaseFormField.COMPATIBLE_MODEL)
        add(GenerationBaseFormField.COMPATIBLE_DIMENSIONS)
        add(GenerationBaseFormField.COMPATIBLE_BATCH)
    }
    add(GenerationBaseFormField.PROMPT)
    when (mode) {
        ServerSource.AUTOMATIC1111,
        ServerSource.SWARM_UI,
        ServerSource.EASY_DIFFUSION,
        ServerSource.HUGGING_FACE,
        ServerSource.STABILITY_AI,
        ServerSource.ARLI_AI,
        ServerSource.LOCAL_MICROSOFT_ONNX,
        ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
        ServerSource.LOCAL_APPLE_CORE_ML,
        ServerSource.LOCAL_APPLE_BONSAI -> add(GenerationBaseFormField.NEGATIVE_PROMPT)

        else -> Unit
    }
    // Preserve the existing empty dimension row's top padding for providers without size controls.
    if (!isCompatibleProvider && (mode != ServerSource.FAL_AI || state.falAiModel.supportsImageSize)) {
        add(GenerationBaseFormField.DIMENSIONS)
    }
    if (mode == ServerSource.OPEN_AI) {
        add(GenerationBaseFormField.OPENAI_QUALITY)
        add(GenerationBaseFormField.OPENAI_BATCH)
    }
    if (state.advancedToggleButtonVisible && mode != ServerSource.OPEN_AI && !isCompatibleProvider) {
        add(GenerationBaseFormField.ADVANCED_TOGGLE)
    }
}

/**
 * Groups basic controls by rendered structure independently of their stable field identity.
 * Prompt editors share one renderer; provider and paired dimension controls retain separate types
 * where their internal structures differ from ordinary dropdowns or advanced numeric editors.
 *
 * @receiver Basic field selected by the provider's visibility rules.
 * @return Structural category used for lazy-slot recycling and prefetch cost estimates.
 * @author Dmitriy Moroz
 */
internal fun GenerationBaseFormField.contentType(): GenerationFormContentType = when (this) {
    GenerationBaseFormField.PROVIDER -> GenerationFormContentType.PROVIDER
    GenerationBaseFormField.SDXL_BACKEND,
    GenerationBaseFormField.BONSAI_BACKEND,
    GenerationBaseFormField.OPENAI_QUALITY -> GenerationFormContentType.DROPDOWN

    GenerationBaseFormField.COMPATIBLE_MODEL -> GenerationFormContentType.COMPATIBLE_MODEL
    GenerationBaseFormField.COMPATIBLE_DIMENSIONS -> GenerationFormContentType.COMPATIBLE_DIMENSIONS
    GenerationBaseFormField.COMPATIBLE_BATCH,
    GenerationBaseFormField.OPENAI_BATCH -> GenerationFormContentType.BATCH_EDITOR

    GenerationBaseFormField.PROMPT,
    GenerationBaseFormField.NEGATIVE_PROMPT -> GenerationFormContentType.PROMPT_EDITOR

    GenerationBaseFormField.DIMENSIONS -> GenerationFormContentType.DIMENSIONS
    GenerationBaseFormField.ADVANCED_TOGGLE -> GenerationFormContentType.ADVANCED_TOGGLE
}
