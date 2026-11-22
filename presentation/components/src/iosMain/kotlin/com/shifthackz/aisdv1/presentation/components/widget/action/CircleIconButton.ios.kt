package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Uses the same native glass circle as toolbar actions, retaining semantic and destructive icon colors.
 * Compose owns taps and accessibility so enclosing contextual-help gestures remain available.
 *
 * @param onClick Receives enabled activation.
 * @param icon Shared action vector.
 * @param contentDescription Localized action announced by accessibility.
 * @param modifier Placement of the 48-point control.
 * @param isEnabled Whether activation is allowed.
 * @param tint Foreground for selected, normal or destructive actions.
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
    PlatformCircleButton(
        onClick, modifier, isEnabled,
        nativeIcon = icon,
        nativeTint = tint,
        nativeDescription = contentDescription,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = if (isEnabled) tint else tint.copy(alpha = 0.38f),
        )
    }
}
