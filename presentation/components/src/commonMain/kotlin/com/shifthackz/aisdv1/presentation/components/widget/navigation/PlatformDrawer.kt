package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Uses the existing opaque Material sheet on Android and a theme-tinted blur on iOS.
 *
 * @param modifier Sheet placement and measurement supplied by the drawer host.
 * @param content Branding and destination rows.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformDrawerSheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)

/**
 * Renders a drawer destination using Material on Android and native glass on iOS.
 * The caller retains selection, navigation and enclosing long-press help.
 *
 * @param label Localized destination label.
 * @param icon Shared destination vector.
 * @param isSelected Whether this destination is active.
 * @param onClick Requests navigation through the root router.
 * @param modifier Row placement.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformDrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
