package com.shifthackz.aisdv1.presentation.app

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * App-shell actions delegated to the existing root navigation controller.
 *
 * @author Dmitriy Moroz
 */
internal sealed interface AiSdAppIntent : MviIntent {
    /**
     * Dismisses the drawer before navigating through the existing Back history.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBack : AiSdAppIntent
}
