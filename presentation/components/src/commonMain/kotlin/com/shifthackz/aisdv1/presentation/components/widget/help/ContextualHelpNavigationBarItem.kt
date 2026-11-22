package com.shifthackz.aisdv1.presentation.components.widget.help

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Keeps equal navigation-item widths around a tooltip anchor and preserves tab selection semantics.
 * Weight belongs to the direct child of the navigation row; the inner row owns the Material item.
 *
 * @param title Localized destination label and help title.
 * @param message Localized purpose of this destination.
 * @param icon Vector shared by navigation and help.
 * @param isSelected Whether this destination is current.
 * @param onClick Requests a destination change on a short tap.
 * @param modifier Placement of this weighted item in its navigation row.
 * @author Dmitriy Moroz
 */
@Composable
fun RowScope.ContextualHelpNavigationBarItem(
    title: String,
    message: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.weight(1f)) {
        ContextualHelp(
            title = title,
            message = message,
            titleStartIcon = icon,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.fillMaxWidth()) {
                NavigationBarItem(
                    selected = isSelected,
                    label = { Text(title, color = LocalContentColor.current) },
                    colors = NavigationBarItemDefaults.colors()
                        .copy(selectedIndicatorColor = MaterialTheme.colorScheme.primary),
                    icon = { Icon(icon, contentDescription = null) },
                    onClick = onClick,
                )
            }
        }
    }
}
