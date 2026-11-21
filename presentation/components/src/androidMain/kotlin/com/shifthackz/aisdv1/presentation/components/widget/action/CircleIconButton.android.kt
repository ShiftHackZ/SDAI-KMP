package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Uses the default Material icon-button interaction and disabled appearance on Android.
 *
 * @param onClick Receives enabled activation.
 * @param icon Shared action vector.
 * @param contentDescription Localized action announced by accessibility.
 * @param modifier Placement of the 48dp touch area.
 * @param isEnabled Whether activation is allowed.
 * @param tint Shared foreground color for the enabled icon.
 * @author Dmitriy Moroz
 */
@Composable
actual fun CircleIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier,
    isEnabled: Boolean,
    tint: Color,
) {
    IconButton(onClick = onClick, modifier = modifier.size(48.dp), enabled = isEnabled) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = if (isEnabled) tint else tint.copy(alpha = 0.38f),
        )
    }
}
