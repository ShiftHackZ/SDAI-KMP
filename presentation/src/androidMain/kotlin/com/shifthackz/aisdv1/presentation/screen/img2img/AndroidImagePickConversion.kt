package com.shifthackz.aisdv1.presentation.screen.img2img

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.shifthackz.aisdv1.core.common.log.errorLog
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.widget.image.OriginalImageBudget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException

/**
 * Converts a picked URI off main using bounded sampling and the existing square/JPEG normalization.
 * Large camera originals are sampled to fit the allocation budget instead of rejected by source size.
 * Every bitmap here is owned solely by conversion and is released before returning encoded content.
 *
 * @receiver Picker context used only for content-resolver streams.
 * @param uri Selected gallery or camera source.
 * @param budget Sampled pixel and final encoded-output admission.
 * @return Selected normalized content, or a localized controlled conversion failure.
 * @throws CancellationException when the calling picker operation is cancelled.
 * @author Dmitriy Moroz
 */
internal suspend fun Context.imageUriToPickResult(
    uri: Uri,
    budget: OriginalImageBudget
): ImageToImagePickResult {
    val operation = currentCoroutineContext()
    var source: Bitmap? = null
    var normalized: Bitmap? = null
    try {
        operation.ensureActive()
        val decoded = decodePickedBitmap(uri, budget) ?: throw IOException("Image decode failed")
        source = decoded
        operation.ensureActive()
        val output = decoded.normalizePickedBitmap()
        normalized = output
        operation.ensureActive()
        return BoundedImageOutputStream(budget.encodedChars) { operation.ensureActive() }.use { stream ->
            if (!output.compress(
                    Bitmap.CompressFormat.JPEG,
                    PICKED_IMAGE_JPEG_QUALITY,
                    stream
                )
            ) throw IOException("Image compression failed")
            operation.ensureActive()
            ImageToImagePickResult.Selected(stream.encoded())
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        operation.ensureActive()
        errorLog("ImageToImagePlatformActions", failure)
        return ImageToImagePickResult.Failed(Localization.string("error_invalid"))
    } finally {
        normalized?.takeIf { it !== source }?.recycle()
        source?.recycle()
    }
}

/**
 * Inspects bounds before allocation and raises the power-of-two sample until both limits fit.
 * Positive dimension arithmetic uses Long, including rounded-up dimensions, to avoid overflow.
 *
 * @receiver Content-resolver owner.
 * @param uri Stable picked source to inspect and decode.
 * @param budget Maximum sampled pixels; original camera pixel counts may exceed it.
 * @return Owned sampled bitmap, or null when the stream cannot be decoded.
 * @author Dmitriy Moroz
 */
private fun Context.decodePickedBitmap(uri: Uri, budget: OriginalImageBudget): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (true) {
        val width = (bounds.outWidth.toLong() + sample - 1L) / sample
        val height = (bounds.outHeight.toLong() + sample - 1L) / sample
        if (maxOf(
                width,
                height
            ) <= MAX_PICKED_IMAGE_SIDE * 2L && width * height <= budget.maxPixels
        ) break
        if (sample > Int.MAX_VALUE / 2) throw IOException("Image dimensions exceed sampling range")
        sample *= 2
    }
    val options = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.ARGB_8888
        inScaled = false
    }
    val decoded =
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: return null
    if (decoded.width.toLong() * decoded.height > budget.maxPixels || maxOf(
            decoded.width,
            decoded.height
        ) > MAX_PICKED_IMAGE_SIDE * 2
    ) {
        decoded.recycle()
        throw IOException("Decoded image exceeds admission")
    }
    return decoded
}

/**
 * Keeps the existing center crop, 1536px maximum edge and JPEG quality semantics.
 * Intermediate crops are released on both successful scaling and exceptional paths.
 *
 * @receiver Owned sampled bitmap; ownership remains with the caller.
 * @return Owned normalized bitmap, which can be identical to the receiver.
 * @author Dmitriy Moroz
 */
private fun Bitmap.normalizePickedBitmap(): Bitmap {
    val side = minOf(width, height)
    val cropped = if (width == side && height == side) this else Bitmap.createBitmap(
        this,
        (width - side) / 2,
        (height - side) / 2,
        side,
        side
    )
    var output: Bitmap? = null
    try {
        output = if (side <= MAX_PICKED_IMAGE_SIDE) cropped else Bitmap.createScaledBitmap(
            cropped,
            MAX_PICKED_IMAGE_SIDE,
            MAX_PICKED_IMAGE_SIDE,
            true
        )
        return output
    } finally {
        if (cropped !== this && cropped !== output) cropped.recycle()
    }
}

private const val MAX_PICKED_IMAGE_SIDE = 1536
private const val PICKED_IMAGE_JPEG_QUALITY = 95
