package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Joins independently composed list items into the existing generation card. Only the first
 * and last segments round their outer corners; adjacent segments share color and horizontal
 * padding without gaps or an inner scrolling container.
 *
 * @param isImg2Img Whether to use the elevated image-to-image form surface.
 * @param isFirst Whether this is the card header and base-input segment.
 * @param isLast Whether this is the status and bottom-padding segment.
 * @param modifier Item placement and appearance animation supplied by the parent lazy list.
 * @param content Controls belonging to this segment, composed only while needed by the list.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationFormCardSegment(
    isImg2Img: Boolean,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val top = if (isFirst) 16.dp else 0.dp
    val bottom = if (isLast) 16.dp else 0.dp
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = top,
            topEnd = top,
            bottomStart = bottom,
            bottomEnd = bottom,
        ),
        color = if (isImg2Img) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isImg2Img) 1.dp else 0.dp,
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = top, bottom = bottom)) {
            content()
        }
    }
}
