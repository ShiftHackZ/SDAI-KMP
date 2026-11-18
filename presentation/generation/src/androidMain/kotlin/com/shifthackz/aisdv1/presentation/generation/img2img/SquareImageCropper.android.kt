package com.shifthackz.aisdv1.presentation.generation.img2img

import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget
import com.shifthackz.aisdv1.core.localization.Localization

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Center-crops an admitted original and normalizes it to at most 1536px JPEG at quality 95.
 * An already square source within that edge keeps its original bytes. The caller dispatches
 * this explicit transform off main; no sampled display bitmap participates in the operation.
 *
 * @param base64 Original encoded image, optionally prefixed by a data URI.
 * @param budget Encoded/pixel admission applied before full bitmap allocation.
 * @return Normalized original image bytes encoded as Base64.
 * @throws IllegalArgumentException when the encoded source or original dimensions exceed admission.
 * @author Dmitriy Moroz
 */
internal actual suspend fun cropBase64ImageToSquare(
    base64: String,
    budget: OriginalImageBudget
): String {
    require(base64.length <= budget.encodedChars) { Localization.string("error_invalid") }
    val raw = base64.substringAfter("base64,", base64).trim()
    val imageBytes = Base64.decode(raw, Base64.DEFAULT)
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
    require(bounds.outWidth > 0 && bounds.outHeight > 0 && bounds.outWidth.toLong() * bounds.outHeight <= budget.maxPixels) {
        Localization.string(
            "error_invalid"
        )
    }
    val source = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        ?: error(Localization.string("error_invalid"))
    if (source.width == source.height && source.width <= MAX_CROPPED_IMAGE_SIDE) {
        return base64
    }

    val size = minOf(source.width, source.height)
    val x = (source.width - size) / 2
    val y = (source.height - size) / 2
    val cropped = Bitmap.createBitmap(source, x, y, size, size)
    val output = if (size > MAX_CROPPED_IMAGE_SIDE) {
        Bitmap
            .createScaledBitmap(cropped, MAX_CROPPED_IMAGE_SIDE, MAX_CROPPED_IMAGE_SIDE, true)
            .also { cropped.recycle() }
    } else {
        cropped
    }

    return ByteArrayOutputStream().use { stream ->
        output.compress(Bitmap.CompressFormat.JPEG, CROPPED_IMAGE_JPEG_QUALITY, stream)
        Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}


private const val MAX_CROPPED_IMAGE_SIDE = 1536


private const val CROPPED_IMAGE_JPEG_QUALITY = 95
