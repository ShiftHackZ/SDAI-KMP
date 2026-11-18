package com.shifthackz.aisdv1.presentation.components.model

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * Shared absence, generic-message or explicit-message error state for presentation renderers.
 *
 * @author Dmitriy Moroz
 */
sealed interface ErrorState : MviState {

    /**
     * No error should be displayed.
     *
     * @author Dmitriy Moroz
     */
    data object None : ErrorState

    /**
     * An error exists without public details; render the localized generic explanation.
     *
     * @author Dmitriy Moroz
     */
    data object Generic : ErrorState

    /**
     * An error with explicitly supplied user-facing details.
     *
     * @param message Localizable explanation rendered by the screen or dialog.
     *
     * @author Dmitriy Moroz
     */
    @Immutable
    data class WithMessage(val message: UiText) : ErrorState
}
