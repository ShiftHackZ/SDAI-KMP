package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Selects one of a small set of peer pages with Material tabs on Android and UIKit segments on iOS.
 * The caller owns selection and pager synchronization; native interaction only requests a new index.
 *
 * @param labels Nonempty ordered localized page titles.
 * @param selectedIndex Current page index, within [labels].
 * @param onSelect Requests a page selection without performing navigation inside the component.
 * @param modifier Bounds and placement of the complete tab strip.
 * @param isEnabled Whether selection is allowed while the owning screen is busy.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformTabBar(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
)
