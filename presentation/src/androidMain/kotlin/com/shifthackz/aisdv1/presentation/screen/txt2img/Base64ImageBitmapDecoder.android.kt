package com.shifthackz.aisdv1.presentation.screen.txt2img

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Decodes the original pixel dimensions for an explicitly opened editor.
 * Dimension admission precedes bitmap rasterization; display previews use the sampled loader.
 * The caller owns background dispatch, encoded-byte admission and decode concurrency.
 *
 * @receiver Original Base64 image, optionally prefixed by a data URI.
 * @param maxPixels Maximum admitted original pixel count.
 * @return Full-resolution image, or null for invalid content or rejected dimensions.
 * @author Dmitriy Moroz
 */
internal actual fun String.decodeBase64ImageBitmap(maxPixels: Long): ImageBitmap? =
    try {
        val raw = substringAfter("base64,", this)
        val bytes = Base64.decode(raw, Base64.DEFAULT)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0 && bounds.outWidth.toLong() * bounds.outHeight <= maxPixels)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
