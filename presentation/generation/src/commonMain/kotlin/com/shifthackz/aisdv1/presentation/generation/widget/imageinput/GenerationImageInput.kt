package com.shifthackz.aisdv1.presentation.generation.widget.imageinput

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import com.shifthackz.aisdv1.presentation.components.widget.imageinput.ImageInputCard
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageInPaintCanvas
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageInPaintState

/**
 * Shared input card, sampled preview, loading state and editing actions for image generation and upscaling.
 *
 * @param image Encoded source's sampled display pixels; null keeps a loading placeholder while decoding.
 * @param hasImage Whether an original image has been selected.
 * @param isPickingImage Whether acquisition or validation is pending.
 * @param isEnabled Whether the owning screen allows image edits.
 * @param onGalleryClick Opens device photos.
 * @param onCameraClick Opens native camera capture.
 * @param onRandomClick Requests one random source image.
 * @param onHistoryImageSelected Receives original bytes selected from the application's Gallery.
 * @param onClearClick Clears the selected source through its state owner.
 * @param modifier Placement of the complete card.
 * @param inPaint Mask preview, absent for operations without mask support.
 * @param onInPaintClick Opens mask editing when supported.
 * @param clearLabel Localized clear action.
 * @param inPaintLabel Localized mask editing action.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationImageInput(
    image: ImageBitmap?, hasImage: Boolean, isPickingImage: Boolean, isEnabled: Boolean,
    onGalleryClick: () -> Unit, onCameraClick: () -> Unit, onRandomClick: () -> Unit,
    onHistoryImageSelected: (String) -> Unit, onClearClick: () -> Unit,
    modifier: Modifier = Modifier, inPaint: ImageInPaintState? = null,
    onInPaintClick: () -> Unit = {},
    clearLabel: String = Localization.string("action_clear"),
    inPaintLabel: String = Localization.string("in_paint_title"),
) {
    ImageInputCard(
        isPickingImage = isPickingImage, isEnabled = isEnabled && !isPickingImage,
        hasImage = hasImage, onGalleryClick = onGalleryClick, onCameraClick = onCameraClick,
        onRandomClick = onRandomClick, onHistoryImageSelected = onHistoryImageSelected,
        modifier = modifier,
    ) {
        if (image == null) Box(
            Modifier.fillMaxWidth().heightIn(max = 420.dp).aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }
        else SelectedImageInput(
            image = image, inPaint = inPaint ?: ImageInPaintState(),
            clearLabel = clearLabel, inPaintLabel = inPaintLabel,
            isEnabled = isEnabled && !isPickingImage, isInPaintVisible = inPaint != null,
            onInPaintClick = onInPaintClick, onClearClick = onClearClick,
        )
    }
}

/**
 * Shows an existing source image and its mask with inpaint and clear actions.
 *
 * @param image Admitted sampled source pixels.
 * @param inPaint Stored mask strokes projected onto the preview.
 * @param clearLabel Localized clear action.
 * @param inPaintLabel Localized mask editing action.
 * @param isEnabled Whether source editing is currently allowed.
 * @param isInPaintVisible Whether this provider exposes mask editing.
 * @param onInPaintClick Opens the full mask editor.
 * @param onClearClick Requests removal of the source image.
 * @param modifier Layout modifiers for the preview/action group.
 *
 * @author Dmitriy Moroz
 */
@Composable
private fun SelectedImageInput(
    image: ImageBitmap,
    inPaint: ImageInPaintState,
    clearLabel: String,
    inPaintLabel: String,
    isEnabled: Boolean,
    isInPaintVisible: Boolean,
    onInPaintClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        val previewModifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).aspectRatio(1f)
        if (isInPaintVisible) {
            ImageInPaintCanvas(
                modifier = previewModifier,
                image = image,
                state = inPaint,
                drawEnabled = false,
                maskAlpha = 1f,
            )
        } else {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = previewModifier,
                contentScale = ContentScale.Fit,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isInPaintVisible) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f),
                    isEnabled = isEnabled,
                    onClick = onInPaintClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = inPaintLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            OutlinedButton(
                modifier = Modifier
                    .weight(1f),
                isEnabled = isEnabled,
                onClick = onClearClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = clearLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

        }
    }
}
