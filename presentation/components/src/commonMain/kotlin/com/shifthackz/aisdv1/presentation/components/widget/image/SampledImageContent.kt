package com.shifthackz.aisdv1.presentation.components.widget.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale

/**
 * Draws admitted pixels inside an independently measured slot; absent pixels leave the same slot empty.
 *
 * @param image Decoded sample, or null while loading or unavailable.
 * @param modifier Independent viewport geometry and interaction.
 * @param contentDescription Accessibility description of the image.
 * @param contentScale Scaling within the viewport without changing its measurement.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SampledImageContent(
    image: ImageBitmap?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    Box(modifier) {
        if (image != null) Image(
            bitmap = image,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.matchParentSize(),
        )
    }
}
