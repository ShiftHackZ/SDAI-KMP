package com.shifthackz.aisdv1.core.imageprocessing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

/**
 * Samples large sources before filtering them to the proportional output size.
 *
 * @author Dmitriy Moroz
 */
internal actual class PlatformImageDownscaler actual constructor() : ImageDownscaler {
    /**
     * Keeps the whole image and avoids decoding the original raster at full resolution.
     *
     * @param image Encoded original pixels.
     * @param maxWidth Positive maximum output width.
     * @param maxHeight Positive maximum output height.
     * @author Dmitriy Moroz
     */
    actual override fun downscale(image: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
        require(maxWidth > 0 && maxHeight > 0)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(image, 0, image.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Invalid input image" }
        if (bounds.outWidth <= maxWidth && bounds.outHeight <= maxHeight) return image
        val (width, height) = downscaledImageDimensions(
            bounds.outWidth,
            bounds.outHeight,
            maxWidth,
            maxHeight
        )
        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= width &&
            bounds.outHeight / (sampleSize * 2) >= height
        ) sampleSize *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = requireNotNull(BitmapFactory.decodeByteArray(image, 0, image.size, options))
        try {
            val scaled = Bitmap.createScaledBitmap(decoded, width, height, true)
            try {
                return ByteArrayOutputStream().use { output ->
                    check(
                        scaled.compress(
                            Bitmap.CompressFormat.PNG,
                            100,
                            output
                        )
                    ) { "Image downscale encoding failed" }
                    output.toByteArray()
                }
            } finally {
                if (scaled !== decoded) scaled.recycle()
            }
        } finally {
            decoded.recycle()
        }
    }
}
