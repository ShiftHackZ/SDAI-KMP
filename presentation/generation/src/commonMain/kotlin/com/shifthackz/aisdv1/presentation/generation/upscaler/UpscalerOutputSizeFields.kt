package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize
import com.shifthackz.aisdv1.presentation.components.theme.textFieldColors

/**
 * Shows predicted output dimensions as selectable read-only fields without opening a keyboard.
 *
 * @param size Expected pixel dimensions; null displays placeholders before input/model selection.
 * @param modifier Placement of the two-field row.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerOutputSizeFields(size: UpscalerImageSize?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = size?.width?.toString() ?: "—", onValueChange = {},
            readOnly = true, singleLine = true, modifier = Modifier.weight(1f),
            label = { Text(Localization.string("width")) }, colors = textFieldColors,
        )
        OutlinedTextField(
            value = size?.height?.toString() ?: "—", onValueChange = {},
            readOnly = true, singleLine = true, modifier = Modifier.weight(1f),
            label = { Text(Localization.string("height")) }, colors = textFieldColors,
        )
    }
}
