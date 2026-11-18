package com.shifthackz.aisdv1.presentation.generation.modal.extras

import com.shifthackz.aisdv1.core.mvi.MviEffect

/**
 * Dismissal or confirmed prompt replacement emitted by a prompt-extra editor.
 *
 * @author Dmitriy Moroz
 */
sealed interface ExtrasEffect : MviEffect {

    /**
     * Returns the confirmed prompt pair to the parent generation form.
     *
     * @param prompt Confirmed positive prompt.
     * @param negativePrompt Confirmed negative prompt.
     *
     * @author Dmitriy Moroz
     */
    data class ApplyPrompts(
        val prompt: String,
        val negativePrompt: String,
    ) : ExtrasEffect


    data object Close : ExtrasEffect
}
