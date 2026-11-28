package com.shifthackz.aisdv1.presentation.home

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp

/**
 * Keeps home destinations reachable on expanded Android windows without occupying the bottom edge.
 * Scrolling preserves all destinations when the window becomes short or text is enlarged.
 *
 * @param items Existing localized destinations, selection and contextual help.
 * @param modifier Placement of the navigation column beside route content.
 * @author Dmitriy Moroz
 */
@Composable
internal fun HomeNavigationRail(
    items: List<HomeNavigationBarItem>,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
        items.forEach { item ->
            ContextualHelp(
                title = item.label,
                message = item.helpMessage,
                titleStartIcon = item.icon
            ) {
                NavigationRailItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.label) },
                )
            }
        }
    }
}
