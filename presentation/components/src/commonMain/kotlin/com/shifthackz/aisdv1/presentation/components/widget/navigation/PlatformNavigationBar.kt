package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shows caller-owned destinations using Material on Android and UIKit's native tab bar on iOS.
 * UIKit supplies system glass on supported versions. Both renderers preserve labels, icons, selection, short-tap
 * callbacks and long-press help, and reserve the persistent bottom system inset exactly once.
 *
 * @param items Ordered destinations; the caller supplies the selected item and all actions.
 * @param modifier Placement of the whole navigation area, including its safe-area padding.
 * @param hasBackground Whether iOS reserves an opaque backing instead of floating above screen content.
 * @param isObscured Whether shared chrome covers the bar, requiring native content below Compose.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformNavigationBar(
    items: List<PlatformNavigationBarItem>,
    modifier: Modifier = Modifier,
    hasBackground: Boolean = true,
    isObscured: Boolean = false,
)
