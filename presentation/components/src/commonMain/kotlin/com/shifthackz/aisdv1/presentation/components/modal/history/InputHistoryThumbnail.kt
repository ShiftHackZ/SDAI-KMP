package com.shifthackz.aisdv1.presentation.components.modal.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * Draws a history sample or its unavailable/loading symbol inside the caller's square viewport.
 *
 * @param image Decoded bounded thumbnail, or null while absent or unavailable.
 * @param modifier Square viewport geometry and rounded clipping supplied by the history row.
 * @author Dmitriy Moroz
 */
@Composable
internal fun InputHistoryThumbnail(image: ImageBitmap?, modifier: Modifier = Modifier) {
    if (image != null) {
        Image(
            bitmap = image,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Default.AutoFixNormal,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
