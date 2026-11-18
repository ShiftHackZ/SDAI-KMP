package com.shifthackz.aisdv1.presentation.components.widget.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Bounds inspect before allocation and power-of-two sampling keep originals out of display memory.
 *
 * @param base64 Budgeted encoded source.
 * @param maxDimension Maximum decoded edge.
 * @author Dmitriy Moroz
 */
internal actual fun decodeSampledGalleryImage(base64: String, maxDimension: Int): ImageBitmap? =
    try {
        val bytes = Base64.decode(base64.substringAfter("base64,", base64), Base64.DEFAULT)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0 && bounds.outWidth.toLong() * bounds.outHeight <= 100_000_000L)
        var sample = 1
        while ((bounds.outWidth + sample - 1) / sample > maxDimension || (bounds.outHeight + sample - 1) / sample > maxDimension) sample *= 2
        val options = BitmapFactory.Options()
            .apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
