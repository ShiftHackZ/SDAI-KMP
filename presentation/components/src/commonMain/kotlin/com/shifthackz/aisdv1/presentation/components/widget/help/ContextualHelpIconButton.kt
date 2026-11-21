package com.shifthackz.aisdv1.presentation.components.widget.help

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Explains an icon action on long press while keeping its short tap and accessible label.
 *
 * @param title Localized action name used by the button and help title.
 * @param message Localized explanation of the action and its consequences.
 * @param icon Vector shared by the button and smaller help-title icon.
 * @param onClick Primary action, invoked only by a short tap.
 * @param modifier Placement of the button and its help anchor.
 * @param isEnabled Whether the primary action is available; help remains available when disabled.
 * @author Dmitriy Moroz
 */
@Composable
fun ContextualHelpIconButton(
    title: String,
    message: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    ContextualHelp(title = title, message = message, titleStartIcon = icon, modifier = modifier) {
        IconButton(onClick = onClick, enabled = isEnabled) {
            Icon(icon, contentDescription = title)
        }
    }
}
