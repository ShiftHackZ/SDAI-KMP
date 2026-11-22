package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpNavigationBarItem

/**
 * Preserves the existing Material geometry, theme and legacy Android system-inset handling.
 *
 * @param items Destinations sharing the bar width equally.
 * @param modifier Bounds of the Material navigation surface.
 * @author Dmitriy Moroz
 */
@Composable
internal fun MaterialNavigationBar(
    items: List<PlatformNavigationBarItem>,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = persistentBottomBarWindowInsets(),
        modifier = modifier,
    ) {
        items.forEach { item ->
            ContextualHelpNavigationBarItem(
                title = item.label,
                message = item.helpMessage,
                icon = item.icon,
                isSelected = item.isSelected,
                onClick = item.onClick,
            )
        }
    }
}
