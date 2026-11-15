package com.shifthackz.aisdv1.presentation.screen.img2img

import com.shifthackz.aisdv1.presentation.widget.image.OriginalImageBudget

import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeJoin
import org.jetbrains.skia.Path
import org.jetbrains.skia.Surface
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
@OptIn(ExperimentalEncodingApi::class)
internal actual suspend fun encodeInPaintMaskBase64(
    imageBase64: String,
    strokes: List<InPaintStroke>,
    budget: OriginalImageBudget,
): String? = try {
    require(imageBase64.length <= budget.encodedChars)
    val raw = imageBase64.substringAfter("base64,", imageBase64).trim()
    val source = Image.makeFromEncoded(Base64.decode(raw))
    require(source.width > 0 && source.height > 0 && source.width.toLong() * source.height <= budget.maxPixels)
    val surface = Surface.makeRasterN32Premul(source.width, source.height)
    val canvas = surface.canvas
    val paint = Paint().apply {
        color = Color.WHITE
        mode = PaintMode.STROKE
        strokeCap = PaintStrokeCap.ROUND
        strokeJoin = PaintStrokeJoin.ROUND
        isAntiAlias = true
    }

    strokes.forEach { stroke ->
        paint.strokeWidth = stroke.brushSize *
                (source.width.toFloat() / stroke.canvasWidth.coerceAtLeast(1))
        canvas.drawPath(stroke.toSkiaPath(source.width, source.height), paint)
    }

    val data = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)
        ?: return null
    Base64.encode(data.bytes)
} catch (_: Exception) {
    null
}

/**
 * Converts SDAI data with `toSkiaPath`.
 *
 * @param targetWidth target width value consumed by the API.
 * @param targetHeight target height value consumed by the API.
 * @return Result produced by `toSkiaPath`.
 * @author Dmitriy Moroz
 */
@Suppress("DEPRECATION", "DEPRECATION_ERROR")
private fun InPaintStroke.toSkiaPath(
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
