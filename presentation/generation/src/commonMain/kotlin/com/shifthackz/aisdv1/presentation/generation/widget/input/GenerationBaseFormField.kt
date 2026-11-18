package com.shifthackz.aisdv1.presentation.generation.widget.input

/**
 * Stable identities for basic provider, prompt and sizing controls. Each lazy key combines its
 * provider with this identity, keeping positive and negative prompt editing state independent.
 *
 * @author Dmitriy Moroz
 */
internal enum class GenerationBaseFormField {
    PROVIDER,
    MNN_BACKEND,
    SDXL_BACKEND,
    BONSAI_BACKEND,
    COMPATIBLE_MODEL,
    COMPATIBLE_DIMENSIONS,
    COMPATIBLE_BATCH,
    PROMPT,
    NEGATIVE_PROMPT,
    DIMENSIONS,
    OPENAI_QUALITY,
    OPENAI_BATCH,
    ADVANCED_TOGGLE,
}
