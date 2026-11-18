package com.shifthackz.aisdv1.presentation.donate

import com.shifthackz.aisdv1.core.mvi.MviEffect

/**
 * External navigation performed by the donation screen host.
 *
 * @author Dmitriy Moroz
 */
sealed interface DonateEffect : MviEffect {
    /**
     * Opens the configured support page in the platform URL handler.
     *
     * @param url Absolute support destination supplied by the application links provider.
     *
     * @author Dmitriy Moroz
     */
    data class OpenUrl(val url: String) : DonateEffect
}
