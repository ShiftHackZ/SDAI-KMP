package com.shifthackz.aisdv1.presentation.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp

/**
 * One destination rendered by the home drawer; the application supplies navigation callbacks.
 *
 * @param label Localized destination label.
 * @param selected Whether this item represents the active route.
 * @param icon Destination icon rendered with the drawer's content color.
 * @param helpMessage Localized explanation shared with other navigation surfaces where applicable.
 * @param onClick Requests navigation through the root router.
 *
 * @author Dmitriy Moroz
 */
data class DrawerSheetItem(
    val label: String,
    val selected: Boolean,
    val icon: ImageVector,
    val helpMessage: String,
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
            ContextualHelp(
                title = item.label,
                message = item.helpMessage,
                titleStartIcon = item.icon,
                modifier = itemModifier.padding(bottom = 4.dp),
            ) {
                NavigationDrawerItem(
                    selected = item.selected,
                    label = {
                        Text(
                            text = item.label,
                            color = LocalContentColor.current,
                        )
                    },
                    icon = { Icon(item.icon, contentDescription = null) },
                    onClick = item.onClick,
                )
            }
        }
    }
}
