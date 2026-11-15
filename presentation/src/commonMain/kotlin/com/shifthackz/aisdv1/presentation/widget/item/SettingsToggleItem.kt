package com.shifthackz.aisdv1.presentation.widget.item

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
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
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsToggleItem(
    text: UiText,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    startIcon: ImageVector? = null,
    textSuffixContent: (@Composable () -> Unit)? = null,
) {
    SettingsItem(
        modifier = modifier,
        loading = isLoading,
        startIcon = startIcon,
        text = text,
        textSuffixContent = textSuffixContent,
        onClick = { onCheckedChange(!isChecked) },
        endValueContent = {
            Switch(
                modifier = Modifier.padding(horizontal = 8.dp),
                checked = isChecked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}
