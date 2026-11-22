package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Retains the existing Android sheet color and default shape/insets.
 *
 * @param modifier Sheet placement.
 * @param content Existing header and destination rows.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformDrawerSheet(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    ModalDrawerSheet(
        modifier,
        drawerContainerColor = MaterialTheme.colorScheme.background,
        content = content
    )
}

/**
 * Retains the existing Material destination styling and selection behavior.
 *
 * @param label Localized destination name.
 * @param icon Existing shared vector.
 * @param isSelected Active destination state.
 * @param onClick Existing navigation callback.
 * @param modifier Destination placement.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformDrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    NavigationDrawerItem(
        selected = isSelected, label = { Text(label, color = LocalContentColor.current) },
        icon = { Icon(icon, contentDescription = null) }, onClick = onClick, modifier = modifier,
    )
}
