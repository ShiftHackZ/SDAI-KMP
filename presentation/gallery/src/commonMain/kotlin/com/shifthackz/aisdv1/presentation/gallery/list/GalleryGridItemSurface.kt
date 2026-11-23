package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Frames a gallery thumbnail with native iOS glass while keeping Android's original cell layout.
 * Image loading, flags and selection remain in the caller's composition.
 *
 * @param borderColor Animated selection accent applied to the frame on iOS or image on Android.
 * @param modifier Cell placement, rounded clipping, selection border and gesture handling.
 * @param content Square thumbnail and overlays; receives the image selection border color.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun GalleryGridItemSurface(
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Color) -> Unit,
)
