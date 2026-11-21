package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Displays a caller-owned Boolean using Material on Android and an accent-tinted native iOS switch.
 * A null callback makes the switch decorative so an enclosing settings row can own activation.
 *
 * @param isChecked Value displayed by the switch; preference ownership stays with the caller.
 * @param onCheckedChange Receives a requested value, or null when the parent handles interaction.
 * @param modifier Layout and accessibility modifiers for the switch area.
 * @param isEnabled Whether switch interaction is allowed.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformSwitch(
    isChecked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
)
