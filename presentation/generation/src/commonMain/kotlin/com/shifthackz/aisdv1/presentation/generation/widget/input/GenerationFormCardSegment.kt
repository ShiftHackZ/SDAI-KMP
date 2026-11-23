package com.shifthackz.aisdv1.presentation.generation.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Joins independently composed list items into the existing generation card. Only the first
 * and last segments round their outer corners; adjacent segments share color and horizontal
 * padding without gaps or an inner scrolling container. The list supplies one shared backdrop,
 * so fields do not mount separate native glass effects or cast shadows at their joins.
 *
 * @param isImg2Img Whether to use the elevated image-to-image form surface.
 * @param modifier Item placement and appearance animation supplied by the parent lazy list.
 * @param isFirst Whether this is the card header and base-input segment.
 * @param isHeader Whether the segment supplies its own header padding and touch area.
 * @param isLast Whether this is the status and bottom-padding segment.
 * @param content Controls belonging to this segment, composed only while needed by the list.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationFormCardSegment(
    isImg2Img: Boolean,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isHeader: Boolean = false,
    isLast: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val top = if (isFirst) 16.dp else 0.dp
    val bottom = if (isLast) 16.dp else 0.dp
    PlatformSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = top,
            topEnd = top,
            bottomStart = bottom,
            bottomEnd = bottom,
        ),
        color = if (isImg2Img) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        shouldDrawBackground = false,
        tonalElevation = if (isImg2Img) 1.dp else 0.dp,
    ) {
        val horizontal = if (isHeader) 0.dp else 16.dp

        Column(
            Modifier.padding(
                start = horizontal,
                end = horizontal,
                top = if (isHeader) 0.dp else top,
                bottom = if (isHeader) 0.dp else bottom
            )
        ) {
            content()
        }

    }
}
