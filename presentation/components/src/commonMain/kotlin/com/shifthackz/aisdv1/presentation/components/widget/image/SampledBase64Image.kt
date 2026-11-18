package com.shifthackz.aisdv1.presentation.components.widget.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged

/**
 * Measures a caller-sized display slot before requesting an asynchronous bounded sample.
 * Decoded content matches the slot without contributing to its measurement, so loading cannot
 * expand the slot or trigger a decode/resize feedback loop.
 *
 * @param base64 Original encoded source.
 * @param modifier Independent nonzero slot geometry (size, aspect ratio or bounded parent fill) and interaction.
 * @param contentDescription Accessibility image description.
 * @param contentScale Display scaling; original pixels remain with the caller.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SampledBase64Image(
    base64: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit
) {
    var edge by remember { mutableIntStateOf(0) }
    val image = rememberPreviewImage(base64, edge)
    Box(modifier.onSizeChanged { edge = maxOf(it.width, it.height) }) {
        if (image != null) Image(
            bitmap = image,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.matchParentSize()
        )
    }
}
