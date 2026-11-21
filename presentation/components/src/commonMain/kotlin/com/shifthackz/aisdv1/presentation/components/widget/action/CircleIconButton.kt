package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Shows a compact circular action with a 48dp touch area and a shared 24dp icon.
 * Android retains the default Material icon button; iOS 26+ adds a native UIKit glass surface.
 * Activation and accessibility remain in Compose so enclosing long-press help retains its gestures.
 *
 * @param onClick Requests the action when the button is enabled.
 * @param icon Shared action vector.
 * @param contentDescription Localized accessible action name, including the current toggle action.
 * @param modifier Placement and optional sizing of the complete button.
 * @param isEnabled Whether short-tap and accessibility activation are allowed.
 * @param tint Shared icon color, allowing selected and destructive actions to follow the theme.
 * @author Dmitriy Moroz
 */
@Composable
expect fun CircleIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    tint: Color = LocalContentColor.current,
)
