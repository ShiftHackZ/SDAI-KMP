package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.LocalUIViewController
import com.shifthackz.aisdv1.presentation.components.widget.navigation.appNavigationHost
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationBarInsetsEnabled

/**
 * Keeps status-bar protection while including UIKit's adaptive top reservation inside shared chrome.
 * A horizontal tab container may report a content guide starting at zero on an ordinary iPhone;
 * taking the larger inset keeps toolbar controls below system indicators without adding both spaces.
 *
 * @return The larger of the system status inset and native content-guide top protection.
 * @author Dmitriy Moroz
 */
@Composable
actual fun persistentTopAppBarWindowInsets(): WindowInsets {
    val host = LocalUIViewController.current.appNavigationHost()
    return if (host != null && LocalNavigationBarInsetsEnabled.current) {
        WindowInsets.statusBars.union(WindowInsets(top = host.topInset))
    } else WindowInsets.statusBars
}
