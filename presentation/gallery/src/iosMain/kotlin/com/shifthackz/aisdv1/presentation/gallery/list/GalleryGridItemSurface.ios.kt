package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Aligns the native glass and square thumbnail to the same bounds and rounded outline.
 * Content retains its sampled loader and gestures; only the decorative backing uses UIKit.
 *
 * @param borderColor Animated selection accent applied to the frame on iOS or image on Android.
 * @param modifier Cell placement, outer clipping, selection border and gestures.
 * @param content Full-size thumbnail and overlays; receives a transparent image border because selection outlines the surface.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun GalleryGridItemSurface(
    borderColor: Color,
    modifier: Modifier,
    content: @Composable BoxScope.(Color) -> Unit,
) {
    PlatformSurface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(4.dp, borderColor),
        shouldClipBackground = true,
        glassInset = 0.dp,
    ) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        ) { content(Color.Transparent) }
    }
}
