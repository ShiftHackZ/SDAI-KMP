package com.shifthackz.aisdv1.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Uses bounds-only Android probing followed by power-of-two sampling; original pixels are never decoded.
 *
 * @author Dmitriy Moroz
 */
internal actual class PlatformBackupImageDecoder actual constructor() : BackupImageDecoder {
    override fun dimensions(bytes: ByteArray): Pair<Long, Long> {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Invalid backup image dimensions" }
        return bounds.outWidth.toLong() to bounds.outHeight.toLong()
    }

    override fun validateSample(bytes: ByteArray, maxEdge: Int) {
        val (width, height) = dimensions(bytes)
        var sample = 1L
        while ((width - 1) / sample + 1 > maxEdge || (height - 1) / sample + 1 > maxEdge) sample *= 2
        require(sample <= Int.MAX_VALUE) { "Invalid backup sample size" }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample.toInt()
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inScaled = false
        }
        val image = requireNotNull(
            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size,
                options
            )
        ) { "Invalid backup image content" }
        try {
            require(image.width in 1..maxEdge && image.height in 1..maxEdge) { "Backup sample exceeds bounds" }
            require(image.allocationByteCount.toLong() <= maxEdge.toLong() * maxEdge * 4) { "Backup sample exceeds byte budget" }
        } finally {
            image.recycle()
        }
    }
}
