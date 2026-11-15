package com.shifthackz.aisdv1.presentation.screen.img2img

import com.shifthackz.aisdv1.presentation.widget.image.OriginalImageBudget
import com.shifthackz.aisdv1.core.localization.Localization

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.Surface
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
@OptIn(ExperimentalEncodingApi::class)
internal actual suspend fun cropBase64ImageToSquare(
    base64: String,
    budget: OriginalImageBudget
): String {
    require(base64.length <= budget.encodedChars) { Localization.string("error_invalid") }
    val raw = base64.substringAfter("base64,", base64).trim()
    val source = Image.makeFromEncoded(Base64.decode(raw))
    require(source.width > 0 && source.height > 0 && source.width.toLong() * source.height <= budget.maxPixels) {
        Localization.string(
            "error_invalid"
        )
    }
    if (source.width == source.height && source.width <= MAX_CROPPED_IMAGE_SIDE) {
        return base64
    }

    val size = minOf(source.width, source.height)
    val outputSize = minOf(size, MAX_CROPPED_IMAGE_SIDE)
    val x = ((source.width - size) / 2).toFloat()
    val y = ((source.height - size) / 2).toFloat()
    val surface = Surface.makeRasterN32Premul(outputSize, outputSize)
    surface.canvas.drawImageRect(
        image = source,
        src = Rect.makeXYWH(x, y, size.toFloat(), size.toFloat()),
        dst = Rect.makeWH(outputSize.toFloat(), outputSize.toFloat()),
        samplingMode = SamplingMode.LINEAR,
        paint = null,
        strict = true,
    )

    val data = surface.makeImageSnapshot()
        .encodeToData(EncodedImageFormat.JPEG, CROPPED_IMAGE_JPEG_QUALITY)
        ?: return base64
    return Base64.encode(data.bytes)
}

/**
 * Exposes the `MAX_CROPPED_IMAGE_SIDE` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val MAX_CROPPED_IMAGE_SIDE = 1536

/**
 * Exposes the `CROPPED_IMAGE_JPEG_QUALITY` value used by the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
private const val CROPPED_IMAGE_JPEG_QUALITY = 95
