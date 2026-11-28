package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.LocalUIViewController
import com.shifthackz.aisdv1.presentation.components.widget.navigation.appNavigationHost
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationBarInsetsEnabled

/**
 * Reserves each route's additional navigation edge beyond UIKit's horizontal system safe area.
 * Standalone routes consume the system insets already applied by their native host.
 *
 * @return Application placement with horizontal safe areas accounted for exactly once.
 * @author Dmitriy Moroz
 */
@Composable
actual fun Modifier.appHorizontalSafeArea(): Modifier {
    val insets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
    val host = LocalUIViewController.current.appNavigationHost()
    return if (host != null && LocalNavigationBarInsetsEnabled.current) {
        windowInsetsPadding(WindowInsets(left = host.leftInset, right = host.rightInset))
            .consumeWindowInsets(insets)
    } else if (host != null) consumeWindowInsets(insets) else windowInsetsPadding(insets)
}
