package com.shifthackz.aisdv1.presentation.generation.modal.tag

import com.shifthackz.aisdv1.core.mvi.MviEffect

/**
 * Dismissal or confirmed prompt replacement emitted by a prompt-extra editor.
 *
 * @author Dmitriy Moroz
 */
sealed interface EditTagEffect : MviEffect {

    /**
     * Closes tag editing without changing the parent form.
     *
     * @author Dmitriy Moroz
     */
    data object Close : EditTagEffect

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
    ) : EditTagEffect
}
