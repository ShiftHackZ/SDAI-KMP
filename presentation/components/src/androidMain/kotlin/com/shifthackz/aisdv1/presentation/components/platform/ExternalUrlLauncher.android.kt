package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.shifthackz.aisdv1.core.common.extensions.openUrl

/**
 * Remembers a launcher bound to the current Android context for native external URL intents.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberExternalUrlLauncher(): ExternalUrlLauncher {
    val context = LocalContext.current
    return remember(context) {
        ExternalUrlLauncher { url -> context.openUrl(url) }
    }
}
