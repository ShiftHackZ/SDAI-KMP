package com.shifthackz.aisdv1.presentation.generation.modal.embedding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Displays an embedding and highlights whether it belongs to the active prompt field.
 * @param item Catalog keyword and its positive/negative prompt membership.
 * @param isPositivePrompt Whether selection targets the positive rather than negative prompt.
 * @param onItemToggle Requests toggling the keyword in the owning sheet.
 * @param modifier Outer list spacing and card bounds.
 * @author Dmitriy Moroz
 */
@Composable
internal fun EmbeddingItemComposable(
    item: EmbeddingItemUi,
    isPositivePrompt: Boolean,
    onItemToggle: (EmbeddingItemUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isApplied =
        (isPositivePrompt && item.isInPrompt) || (!isPositivePrompt && item.isInNegativePrompt)
    PlatformSurface(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth()
            .clickable { onItemToggle(item) },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isApplied) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = item.keyword,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
