package com.shifthackz.aisdv1.presentation.generation.modal.tag

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Tag-editor initialization, text/weight edits and confirmation requests.
 *
 * @author Dmitriy Moroz
 */
sealed interface EditTagIntent : MviIntent {

    /**
     * Closes the editor without applying a replacement.
     *
     * @author Dmitriy Moroz
     */
    data object Close : EditTagIntent

    /**
     * Initializes the captured prompt pair and selected tag.
     *
     * @param prompt Positive prompt before editing.
     * @param negativePrompt Negative prompt before editing.
     * @param tag Exact source tag to replace or delete.
     * @param isNegative Whether the source tag belongs to the negative prompt.
     *
     * @author Dmitriy Moroz
     */
    data class InitialData(
        val prompt: String,
        val negativePrompt: String,
        val tag: String,
        val isNegative: Boolean,
    ) : EditTagIntent

    /**
     * Replaces the editable plain-tag text.
     *
     * @param tag Current user-edited tag text.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateTag(val tag: String) : EditTagIntent

    /**
     * Replaces the numeric weight of a recognized extra tag.
     *
     * @param value New LoRA/hypernetwork weight.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateValue(val value: Double) : EditTagIntent

    /**
     * Increments or decrements the recognized extra weight by the configured bounded step.
     *
     * @author Dmitriy Moroz
     */
    enum class Value : EditTagIntent {
        Increment, Decrement;
    }

    /**
     * Confirms the replacement or deletes the original tag from its captured prompt.
     *
     * @author Dmitriy Moroz
     */
    enum class Action : EditTagIntent {
        Apply, Delete;
    }
}
