package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Preserves secondary Material tabs, including their indicator, divider and selected accent.
 *
 * @param labels Ordered localized page titles.
 * @param selectedIndex Current page index, within [labels].
 * @param onSelect Requests a page from the shared owner.
 * @param modifier Placement of the tab strip.
 * @param isEnabled Whether tab selection is allowed.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformTabBar(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    isEnabled: Boolean,
) {
    MaterialTabBar(labels, selectedIndex, onSelect, modifier, isEnabled)
}
