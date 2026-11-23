package com.shifthackz.aisdv1.presentation.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata
import com.shifthackz.aisdv1.presentation.components.widget.image.DecodedImageBitmap
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.rememberGalleryImage

/**
 * Samples a revision-aware history image at its measured edge or shows the shared placeholder.
 * Loading and unavailable images use the same placeholder; no image work starts without a loader.
 *
 * @param item Persisted identity and image revision to request.
 * @param imageUnavailable Accessible label for the placeholder.
 * @param modifier Card-controlled thumbnail size and aspect ratio.
 * @param imageLoader Sampled display cache supplied by the application host.
 * @author Dmitriy Moroz
 */
@Composable
internal fun HistoryThumbnail(
    item: GalleryItemMetadata,
    imageUnavailable: String,
    modifier: Modifier = Modifier,
    imageLoader: GalleryImageLoader? = null,
) {
    var imageEdge by remember(item.id) { mutableIntStateOf(0) }
    val decoded = if (imageLoader != null) rememberGalleryImage(
        imageLoader,
        item.id,
        item.imageRevision,
        edge = imageEdge,
    ).value else DecodedImageBitmap.Unavailable
    val imageBitmap = (decoded as? DecodedImageBitmap.Ready)?.image
    val imageModifier = modifier.onSizeChanged { imageEdge = maxOf(it.width, it.height) }
    if (imageBitmap != null) {
        Image(
            modifier = imageModifier,
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(modifier = imageModifier, contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Default.AutoFixNormal,
                contentDescription = imageUnavailable,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
