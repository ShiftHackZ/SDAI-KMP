package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Remembers the UIKit external URL launcher for the current composition.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberExternalUrlLauncher(): ExternalUrlLauncher = remember {
    ExternalUrlLauncher { url -> IosUrlOpener.openUrl(url) }
}
