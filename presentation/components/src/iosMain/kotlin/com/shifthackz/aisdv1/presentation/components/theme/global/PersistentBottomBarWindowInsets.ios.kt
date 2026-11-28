package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.LocalUIViewController
import com.shifthackz.aisdv1.presentation.components.widget.navigation.appNavigationHost
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationBarInsetsEnabled

/**
 * Reserves the native bottom obstruction while allowing bars to move to a side edge.
 * The adaptive host reports remaining native protection or floating-bar clearance. Compose's
 * corner-adapted inset can otherwise reserve extra space after controls have moved clear of corners.
 * Older hosts retain the existing navigation-bar inset and home-indicator protection.
 *
 * @return Native remaining bottom space, or the embedded bar's existing platform inset.
 * @author Dmitriy Moroz
 */
@Composable
actual fun persistentBottomBarWindowInsets(): WindowInsets {
    val host = LocalUIViewController.current.appNavigationHost()
    return if (host != null) {
        WindowInsets(bottom = if (LocalNavigationBarInsetsEnabled.current) host.bottomInset else host.systemBottomInset)
    } else WindowInsets.navigationBars
}
