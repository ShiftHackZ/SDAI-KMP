package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSection
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSubtree
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.rememberGalleryImage
import com.shifthackz.aisdv1.presentation.components.widget.image.DecodedImageBitmap

/**
 * Renders one sampled gallery preview with hidden/favorite flags and caller-owned selection.
 *
 * @param modifier Cell size, selection animation and placement.
 * @param item Persistent image identity, content revision and hidden/favorite flags.
 * @param imageLoader Shared sampled loader; null renders the unavailable-image placeholder.
 * @param checked Whether this identity is selected.
 * @param onClick Opens detail when multi-selection is inactive.
 * @param onLongClick Requests entering selection mode from a normal grid cell.
 * @param onCheckedChange Reports toggling the current selection while selection mode is active.
 * @param selectionMode Whether activation toggles selection instead of opening detail.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun GalleryUiItem(
    modifier: Modifier = Modifier,
    item: GalleryGridItemUi,
    imageLoader: GalleryImageLoader? = null,
    checked: Boolean = false,
    onClick: (GalleryGridItemUi) -> Unit = {},
    onLongClick: () -> Unit = {},
    onCheckedChange: (Boolean) -> Unit = {},
    selectionMode: Boolean = false,
) {
    UiDiagnosticSection(UiDiagnosticSubtree.GALLERY_GRID_IMAGE) {
        val shape = RoundedCornerShape(12.dp)
        val borderColor by animateColorAsState(
            targetValue = if (selectionMode && checked) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
            label = "border_color",
        )
        var imageEdge by remember(item.id) { mutableIntStateOf(0) }
        val decodedImage = if (imageLoader != null) rememberGalleryImage(
            imageLoader,
            item.id,
            item.imageRevision,
            edge = imageEdge
        ).value else DecodedImageBitmap.Unavailable
        GalleryGridItemSurface(
            borderColor = borderColor,
            modifier = modifier
                .onSizeChanged { imageEdge = maxOf(it.width, it.height) }
                .clip(shape)
                .combinedClickable(
                    onLongClick = if (!selectionMode) onLongClick else null,
                    onClick = {
                        if (!selectionMode) {
                            onClick(item)
                        } else {
                            onCheckedChange(!checked)
                        }
                    },
                ),
        ) { imageBorderColor ->
            when (val image = decodedImage) {
                DecodedImageBitmap.Loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .border(width = 4.dp, color = imageBorderColor, shape = shape)
                        .shimmer(),
                )

                is DecodedImageBitmap.Ready -> Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .border(width = 4.dp, color = imageBorderColor, shape = shape)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .border(width = 4.dp, color = imageBorderColor, shape = shape),
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
            if (item.hidden) {
                Icon(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center),
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            if (item.liked) {
                Icon(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .align(Alignment.TopStart),
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            if (selectionMode) {
                val checkBoxShape = RoundedCornerShape(4.dp)
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .clip(checkBoxShape)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            shape = checkBoxShape,
                        ),
                ) {
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = onCheckedChange,
                            colors = CheckboxDefaults.colors(
                                uncheckedColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shows a square shimmer placeholder while gallery metadata/pixels are loading.
 *
 * @param modifier Size and placement of the placeholder cell.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryUiItemShimmer(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .shimmer(),
    )
}

/**
 * Explains the empty gallery state before any saved generation is available.
 *
 * @param modifier Layout modifiers for the centered empty-state text.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = Localization.string("gallery_empty_title"),
            fontSize = 20.sp,
        )
        Text(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = Localization.string("gallery_empty_sub_title"),
            textAlign = TextAlign.Center,
        )
    }
}
