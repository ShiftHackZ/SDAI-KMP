package com.shifthackz.aisdv1.presentation.generation.widget.input

/**
 * Structural categories shared by compatible generation list items for composition recycling
 * and prefetch timing estimates. These are independent of the stable keys that own field state.
 *
 * @author Dmitriy Moroz
 */
internal enum class GenerationFormContentType {
    HEADER,
    PROVIDER,
    COMPATIBLE_MODEL,
    COMPATIBLE_DIMENSIONS,
    BATCH_EDITOR,
    PROMPT_EDITOR,
    DIMENSIONS,
    ADVANCED_TOGGLE,
    FOOTER,
    SEED_EDITOR,
    SLIDER_EDITOR,
    SWITCH,
    DROPDOWN,
    SAMPLER,
    MULTISELECT,
    SIMPLE_SLIDER,
    NOTICE_ACTIONS,
}
