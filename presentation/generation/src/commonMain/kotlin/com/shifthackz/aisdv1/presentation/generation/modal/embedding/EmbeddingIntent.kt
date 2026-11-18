package com.shifthackz.aisdv1.presentation.generation.modal.embedding

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Prompt-polarity selection and embedding toggles emitted by the textual-inversion editor.
 *
 * @author Dmitriy Moroz
 */
sealed interface EmbeddingIntent : MviIntent {

    /**
     * Closes the editor without applying its proposed prompt changes.
     *
     * @author Dmitriy Moroz
     */
    data object Close : EmbeddingIntent

    /**
     * Selects the prompt whose embedding toggles are being edited.
     *
     * @param flag True for the positive prompt, false for the negative prompt.
     *
     * @author Dmitriy Moroz
     */
    data class ChangeSelector(val flag: Boolean) : EmbeddingIntent

    /**
     * Toggles one embedding in the currently selected prompt.
     *
     * @param item Embedding identity and current prompt-selection state.
     *
     * @author Dmitriy Moroz
     */
    data class ToggleItem(val item: EmbeddingItemUi) : EmbeddingIntent

    /**
     * Confirms the edited prompt pair for application by the parent form.
     *
     * @author Dmitriy Moroz
     */
    data object ApplyNewPrompts : EmbeddingIntent
}
