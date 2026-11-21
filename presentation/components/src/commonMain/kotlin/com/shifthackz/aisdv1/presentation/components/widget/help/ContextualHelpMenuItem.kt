package com.shifthackz.aisdv1.presentation.components.widget.help

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Explains a menu command before it is chosen, retaining the menu's ordinary short-tap action.
 *
 * @param title Localized command name.
 * @param message Localized explanation of the command and confirmation behavior.
 * @param icon Vector shared by the leading menu icon and help title.
 * @param onClick Chooses the command; long press opens help instead.
 * @param modifier Placement of the complete menu row.
 * @author Dmitriy Moroz
 */
@Composable
fun ContextualHelpMenuItem(
    title: String,
    message: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ContextualHelp(
        title = title,
        message = message,
        titleStartIcon = icon,
        modifier = modifier.fillMaxWidth()
    ) {
        DropdownMenuItem(
            text = { Text(title) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            onClick = onClick,
        )
    }
}
