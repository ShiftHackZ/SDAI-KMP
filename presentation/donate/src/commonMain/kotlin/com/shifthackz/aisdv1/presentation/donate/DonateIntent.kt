package com.shifthackz.aisdv1.presentation.donate

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Navigation and support-link requests from the donation screen.
 *
 * @author Dmitriy Moroz
 */
sealed interface DonateIntent : MviIntent {

    /**
     * Returns to the previous route.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBack : DonateIntent

    /**
     * Requests opening the configured external support page.
     *
     * @author Dmitriy Moroz
     */
    data object LaunchDonate : DonateIntent
}
