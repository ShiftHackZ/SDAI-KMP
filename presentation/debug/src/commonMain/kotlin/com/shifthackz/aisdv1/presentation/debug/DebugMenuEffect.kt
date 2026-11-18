package com.shifthackz.aisdv1.presentation.debug

import com.shifthackz.aisdv1.core.mvi.MviEffect

/**
 * Transient feedback emitted after a requested debug operation completes.
 *
 * @author Dmitriy Moroz
 */
sealed interface DebugMenuEffect : MviEffect {
    /**
     * A localized success or failure message for the host to present.
     *
     * @param message Feedback text already localized by the ViewModel.
     *
     * @author Dmitriy Moroz
     */
    data class Message(val message: String) : DebugMenuEffect
}
