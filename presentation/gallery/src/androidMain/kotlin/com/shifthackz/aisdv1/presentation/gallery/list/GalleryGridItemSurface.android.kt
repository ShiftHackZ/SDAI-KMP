package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Retains the original Android gallery cell without adding a decorative frame or content inset.
 *
 * @param borderColor Animated selection accent applied to the frame on iOS or image on Android.
 * @param modifier Cell placement, clipping, selection border and gestures.
 * @param content Original thumbnail and overlays; receives the existing selection border color, including image blur behavior.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun GalleryGridItemSurface(
    borderColor: Color,
    modifier: Modifier,
    content: @Composable BoxScope.(Color) -> Unit,
) {
    Box(modifier = modifier) { content(borderColor) }
}
