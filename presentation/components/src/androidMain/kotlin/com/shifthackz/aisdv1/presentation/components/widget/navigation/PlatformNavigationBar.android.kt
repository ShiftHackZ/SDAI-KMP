package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Keeps Android's existing Material bar and shared destination interactions.
 *
 * @param items Caller-owned destination labels, selection and callbacks.
 * @param modifier Placement of the navigation surface.
 * @param hasBackground iOS backing preference; Android retains its opaque Material surface.
 * @param isObscured Shared chrome visibility; Material already participates in Compose drawing order.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformNavigationBar(
    items: List<PlatformNavigationBarItem>,
    modifier: Modifier,
    hasBackground: Boolean,
    isObscured: Boolean,
) {
    MaterialNavigationBar(items, modifier)
}
