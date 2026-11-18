package com.shifthackz.aisdv1.presentation.components.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlin.io.encoding.Base64
import org.jetbrains.skia.Image as SkiaImage

/**
 * Decodes the original pixel dimensions for an explicitly opened editor.
 * Dimension admission precedes bitmap rasterization; display previews use the sampled loader.
 * The caller owns background dispatch, encoded-byte admission and decode concurrency.
 *
 * @receiver Original Base64 image, optionally prefixed by a data URI.
 *
 * @param maxPixels Maximum admitted original pixel count.
 *
 * @return Full-resolution image, or null for invalid content or rejected dimensions.
 *
 * @author Dmitriy Moroz
 */
internal actual fun String.decodeBase64ImageBitmap(maxPixels: Long): ImageBitmap? =
    try {
        val raw = substringAfter("base64,", this)
        val bytes = Base64.decode(raw)
        val image = SkiaImage.makeFromEncoded(bytes)
        require(image.width > 0 && image.height > 0 && image.width.toLong() * image.height <= maxPixels)
        image.toComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
