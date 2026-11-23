package com.shifthackz.aisdv1.presentation.generation.modal.extras

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Displays one LoRA or hypernetwork entry with its current application state and optional weight.
 *
 * @param modifier Card placement within the catalog list.
 * @param item Catalog entry and caller-owned selection state.
 * @param onLoraSelected Requests toggling this entry through the owning sheet.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun ExtrasItemComposable(
    modifier: Modifier = Modifier,
    item: ExtraItemUi,
    onLoraSelected: (ExtraItemUi) -> Unit,
) {
    PlatformSurface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (item.isApplied) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary
        ) else null,
        onClick = { onLoraSelected(item) },
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.DataExploration,
                contentDescription = Localization.string("title_lora_item", item.name),
            )
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = item.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                item.alias?.let {
                    Text(
                        text = Localization.string("label_alias", it),
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            item.value?.let {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier.requiredWidth(IntrinsicSize.Min),
                    maxLines = 1,
                    text = it,
                )
            }
        }
    }
}
