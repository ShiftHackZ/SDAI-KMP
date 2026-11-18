package com.shifthackz.aisdv1.presentation.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * One destination rendered by the home drawer; the application supplies navigation callbacks.
 *
 * @param label Localized destination label.
 * @param selected Whether this item represents the active route.
 * @param icon Destination icon rendered with the drawer's content color.
 * @param onClick Requests navigation through the root router.
 *
 * @author Dmitriy Moroz
 */
data class DrawerSheetItem(
    val label: String,
    val selected: Boolean,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit,
)

/**
 * Renders drawer destinations beneath a caller-provided branding header.
 *
 * @param items Ordered navigation destinations; selection and actions remain caller-owned.
 * @param header Branding and build information shown above the destinations.
 * @param modifier Layout modifiers for the drawer sheet.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun DrawerSheetContent(
    items: List<DrawerSheetItem>,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        val itemModifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        header()

        Spacer(modifier = Modifier.height(32.dp))
        items.forEach { item ->
            NavigationDrawerItem(
                modifier = itemModifier.padding(bottom = 4.dp),
                selected = item.selected,
                label = {
                    Text(
                        text = item.label,
                        color = LocalContentColor.current,
                    )
                },
                icon = item.icon,
                onClick = item.onClick,
            )
        }
    }
}
