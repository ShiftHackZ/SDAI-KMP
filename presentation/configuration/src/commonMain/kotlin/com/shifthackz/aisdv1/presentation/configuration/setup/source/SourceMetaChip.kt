package com.shifthackz.aisdv1.presentation.configuration.setup.source

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Renders a compact provider readiness or version label inside its metadata row.
 *
 * @param text Localized label.
 * @param containerColor Background identifying the metadata category.
 * @param contentColor Foreground contrasted with the category background.
 * @param modifier Placement within the provider metadata flow.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceMetaChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .padding(4.dp)
            .background(
                color = containerColor,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.W500,
        color = contentColor,
    )
}
