package com.shifthackz.aisdv1.presentation.generation.modal.extras

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Selection and confirmation requests from the LoRA/hypernetwork catalog.
 *
 * @author Dmitriy Moroz
 */
sealed interface ExtrasIntent : MviIntent {

    /**
     * Toggles a catalog item in the positive prompt.
     *
     * @param item Extra identity and its current selection state.
     *
     * @author Dmitriy Moroz
     */
    data class ToggleItem(val item: ExtraItemUi) : ExtrasIntent

    /**
     * Confirms the proposed prompt pair for the parent generation form.
     *
     * @author Dmitriy Moroz
     */
    data object ApplyPrompts : ExtrasIntent

    /**
     * Closes catalog selection without applying changes.
     *
     * @author Dmitriy Moroz
     */
    data object Close : ExtrasIntent
}
