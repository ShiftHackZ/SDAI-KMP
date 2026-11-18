package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.Composable

/**
 * Opens external URLs through the native host instead of embedding them in shared screen content.
 *
 * @author Dmitriy Moroz
 */
fun interface ExternalUrlLauncher {
    /**
     * Requests that the platform open an external destination.
     *
     * @param url Absolute URL passed to the native application/browser handler.
     *
     * @author Dmitriy Moroz
     */
    fun openUrl(url: String)
}

/**
 * Remembers a platform URL launcher for the current composition/host context.
 *
 * @author Dmitriy Moroz
 */
@Composable
expect fun rememberExternalUrlLauncher(): ExternalUrlLauncher
