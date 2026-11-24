package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Displays a compact option in a filter sheet using the active and disabled theme colors.
 *
 * @param label Text identifying the filter option.
 * @param isSelected Whether this filter is applied.
 * @param onClick Requests changing the filter selection.
 * @param modifier Placement within the sheet.
 * @param isEnabled Whether this option can be selected.
 * @author Dmitriy Moroz
 */
@Composable
fun FilterOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val shape = RoundedCornerShape(8.dp)
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = when {
        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor = when {
        !isEnabled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.48f)
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        enabled = isEnabled,
        onClick = onClick,
        modifier = modifier.semantics { selected = isSelected },
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
