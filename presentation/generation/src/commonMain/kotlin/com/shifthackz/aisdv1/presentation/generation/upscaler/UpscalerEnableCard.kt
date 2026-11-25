package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformSwitch
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Toggles the optional final stage from the whole card, with one accessible switch action.
 *
 * @param isChecked Whether this generation request includes upscaling.
 * @param isEnabled Whether this screen currently accepts edits.
 * @param onCheckedChange Updates the captured optional stage.
 * @param modifier Placement of the enable card.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerEnableCard(
    isChecked: Boolean, isEnabled: Boolean, onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    PlatformSurface(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .toggleable(
                value = isChecked,
                enabled = isEnabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                Localization.string("upscaler_pipeline_toggle"), modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            PlatformSwitch(
                isChecked = isChecked,
                isEnabled = isEnabled,
                onCheckedChange = null
            )
        }
    }
}
