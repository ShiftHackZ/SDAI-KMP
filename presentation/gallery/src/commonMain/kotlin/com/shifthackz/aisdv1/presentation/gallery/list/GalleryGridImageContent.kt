package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.presentation.components.widget.image.DecodedImageBitmap

/**
 * Displays square sampled pixels, loading shimmer or the unavailable-image placeholder for a cell.
 * Hidden-image blur and selection-border geometry are shared by runtime and fixed-data renderers.
 *
 * @param item Gallery metadata determining hidden-image treatment.
 * @param decodedImage Sampled pixels or their loading/unavailable state.
 * @param borderColor Selection border supplied by the cell surface.
 * @param modifier Placement of the square image within its gallery cell.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryGridImageContent(
    item: GalleryGridItemUi,
    decodedImage: DecodedImageBitmap,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    when (val image = decodedImage) {
        DecodedImageBitmap.Loading -> Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(width = 4.dp, color = borderColor, shape = shape)
                .shimmer(),
        )

        is DecodedImageBitmap.Ready -> Image(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(width = 4.dp, color = borderColor, shape = shape)
                .then(
                    if (!item.hidden) Modifier
                    else Modifier.graphicsLayer {
                        renderEffect = BlurEffect(
                            radiusX = 100f,
                            radiusY = 100f,
                        )
                    }
                ),
            bitmap = image.image,
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )

        DecodedImageBitmap.Unavailable -> Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(width = 4.dp, color = borderColor, shape = shape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Default.AutoFixNormal,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
