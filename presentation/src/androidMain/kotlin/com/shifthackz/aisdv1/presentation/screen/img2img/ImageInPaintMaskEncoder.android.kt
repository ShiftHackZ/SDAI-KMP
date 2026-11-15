package com.shifthackz.aisdv1.presentation.screen.img2img

import com.shifthackz.aisdv1.presentation.widget.image.OriginalImageBudget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Builds a PNG mask at the original image dimensions without resizing the generation input.
 * White strokes are scaled from their recorded canvas coordinates onto a transparent raster.
 * The caller dispatches this explicit transform off main.
 *
 * @param imageBase64 Original encoded image used to establish exact output dimensions.
 * @param strokes Brush paths and the canvas dimensions where their points were recorded.
 * @param budget Encoded/pixel admission before allocating the mask.
 * @return Base64 PNG mask, or null for invalid content or a rejected admission limit.
 * @author Dmitriy Moroz
 */
internal actual suspend fun encodeInPaintMaskBase64(
    imageBase64: String,
    strokes: List<InPaintStroke>,
    budget: OriginalImageBudget,
): String? = try {
    require(imageBase64.length <= budget.encodedChars)
    val raw = imageBase64.substringAfter("base64,", imageBase64)
    val imageBytes = Base64.decode(raw, Base64.DEFAULT)
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
    require(bounds.outWidth > 0 && bounds.outHeight > 0 && bounds.outWidth.toLong() * bounds.outHeight <= budget.maxPixels)
    val bitmap = Bitmap.createBitmap(bounds.outWidth, bounds.outHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    strokes.forEach { stroke ->
        paint.strokeWidth = stroke.brushSize *
                (bounds.outWidth.toFloat() / stroke.canvasWidth.coerceAtLeast(1))
        canvas.drawPath(stroke.toAndroidPath(bounds.outWidth, bounds.outHeight), paint)
    }

    ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
} catch (_: Exception) {
    null
}

/**
 * Converts SDAI data with `toAndroidPath`.
 *
 * @param targetWidth target width value consumed by the API.
 * @param targetHeight target height value consumed by the API.
 * @return Result produced by `toAndroidPath`.
 * @author Dmitriy Moroz
 */
private fun InPaintStroke.toAndroidPath(
    targetWidth: Int,
    targetHeight: Int,
): Path {
    val path = Path()
    val first = points.firstOrNull() ?: return path
    val scaleX = targetWidth.toFloat() / canvasWidth.coerceAtLeast(1)
    val scaleY = targetHeight.toFloat() / canvasHeight.coerceAtLeast(1)
    var previousX = first.x * scaleX
    var previousY = first.y * scaleY
    path.moveTo(previousX, previousY)
    points.drop(1).forEach { point ->
        val x = point.x * scaleX
        val y = point.y * scaleY
        path.quadTo(
            previousX,
            previousY,
            (previousX + x) / 2f,
            (previousY + y) / 2f,
        )
        previousX = x
        previousY = y
    }
    path.lineTo(previousX, previousY)
    return path
}
