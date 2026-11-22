package com.shifthackz.aisdv1.presentation.components.widget.help

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifthackz.aisdv1.presentation.components.widget.action.CircleIconButton

/**
 * Adds persistent long-press help to a platform circle action while preserving its short tap.
 *
 * @param title Localized action name used by accessibility and the help title.
 * @param message Explanation of the action and its consequences.
 * @param icon Vector shared by the circle button and help heading.
 * @param onClick Requests the primary action on a short tap.
 * @param modifier Placement of the complete button and help anchor.
 * @param isEnabled Whether the primary action is available; help remains available when disabled.
 * @param tint Foreground color for normal, selected or destructive actions.
 * @author Dmitriy Moroz
 */
@Composable
fun ContextualHelpCircleIconButton(
    title: String,
    message: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    ContextualHelp(title = title, message = message, titleStartIcon = icon, modifier = modifier) {
        CircleIconButton(
            onClick = onClick,
            icon = icon,
            contentDescription = title,
            isEnabled = isEnabled,
            tint = tint,
        )
    }
}
