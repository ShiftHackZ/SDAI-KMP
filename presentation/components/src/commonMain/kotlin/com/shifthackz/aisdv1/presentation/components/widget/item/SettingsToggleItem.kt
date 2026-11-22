package com.shifthackz.aisdv1.presentation.components.widget.item

import androidx.compose.foundation.layout.padding
import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformSwitch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.UiText

/**
 * Displays a persisted Boolean preference without capturing the owning screen's whole state.
 * The row and switch deliver the requested value through the same callback; the switch consumes
 * its own click, so either touch target performs one operation. No local optimistic value is kept.
 *
 * @param text Localized preference label.
 * @param isChecked Current value supplied by the state owner.
 * @param onCheckedChange Receives the newly requested preference value.
 * @param modifier Layout and styling applied to the row.
 * @param isLoading Whether to show the existing preference placeholder.
 * @param startIcon Optional leading preference icon.
 * @param textSuffixContent Optional badge displayed after the label.
 * @param isEnabled Whether the row and native switch accept changes.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SettingsToggleItem(
    text: UiText,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    startIcon: ImageVector? = null,
    textSuffixContent: (@Composable () -> Unit)? = null,
    isEnabled: Boolean = true,
) {
    SettingsItem(
        modifier = modifier,
        loading = isLoading,
        enabled = isEnabled,
        startIcon = startIcon,
        text = text,
        textSuffixContent = textSuffixContent,
        onClick = { onCheckedChange(!isChecked) },
        endValueContent = {
            PlatformSwitch(
                modifier = Modifier.padding(horizontal = 8.dp),
                isChecked = isChecked,
                onCheckedChange = onCheckedChange,
                isEnabled = isEnabled,
            )
        },
    )
}
