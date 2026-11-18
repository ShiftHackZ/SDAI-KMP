package com.shifthackz.aisdv1.presentation.web

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Application navigation requests from the embedded WebUI route.
 *
 * @author Dmitriy Moroz
 */
sealed interface WebUiIntent : MviIntent {
    /**
     * Leaves the WebUI route after native history has been handled by the screen.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBack : WebUiIntent
}
