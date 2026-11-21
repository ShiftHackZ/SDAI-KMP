package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Preserves Android's Material switch sizing, colors, gestures and semantics.
 *
 * @param isChecked Caller-owned displayed value.
 * @param onCheckedChange Requested-value callback, or null for a decorative row accessory.
 * @param modifier Layout and semantics applied to the switch.
 * @param isEnabled Whether the switch accepts interaction.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformSwitch(
    isChecked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    isEnabled: Boolean,
) {
    Switch(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = isEnabled
    )
}
