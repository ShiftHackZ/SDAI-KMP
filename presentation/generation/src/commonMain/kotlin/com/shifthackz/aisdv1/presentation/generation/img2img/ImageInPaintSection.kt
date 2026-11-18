package com.shifthackz.aisdv1.presentation.generation.img2img

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization


/**
 * Inpaint editor sections for drawing and request-parameter adjustment.
 *
 * @param label Localized tab label captured when the enum is initialized.
 *
 * @author Dmitriy Moroz
 */
internal enum class ImageInPaintTab(
    val label: String,
) {
    Draw(Localization.string("in_paint_tab_1")),
    Adjust(Localization.string("in_paint_tab_2")),
}

/**
 * Projects stored mask strokes over source pixels and optionally captures drawing or viewport transforms.
 * New strokes retain canvas dimensions so encoding can scale them to original image coordinates.
 *
 * @param modifier Size and placement of the square drawing viewport.
 * @param image Source pixels displayed underneath the mask.
 * @param state Persisted stroke list and current brush size.
 * @param drawEnabled Whether a single-finger gesture draws mask strokes.
 * @param maskAlpha Opacity used for completed mask strokes in this preview.
 * @param zoom Current viewport scale, independent of stored stroke coordinates.
 * @param pan Current viewport translation in display pixels.
 * @param transformEnabled Whether transform gestures may update zoom and pan.
 * @param onZoomChange Publishes viewport scale changes to the editor composition.
 * @param onPanChange Publishes viewport translation changes to the editor composition.
 * @param onStrokeDrawn Receives a completed stroke in untransformed canvas coordinates.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImageInPaintCanvas(
    modifier: Modifier = Modifier,
    image: ImageBitmap,
    state: ImageInPaintState,
    drawEnabled: Boolean = true,
    maskAlpha: Float = 1f,
    zoom: Float = 1f,
    pan: Offset = Offset.Zero,
    transformEnabled: Boolean = false,
    onZoomChange: (Float) -> Unit = {},
    onPanChange: (Offset) -> Unit = {},
    onStrokeDrawn: (InPaintStroke) -> Unit = {},
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var activeStroke by remember { mutableStateOf<InPaintStroke?>(null) }
    val currentZoom by rememberUpdatedState(zoom)
    val currentPan by rememberUpdatedState(pan)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = pan.x
                    translationY = pan.y
                    transformOrigin = TransformOrigin.Center
                },
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
            Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                val drawStroke: (InPaintStroke, Color) -> Unit = { stroke, color ->
                    drawPath(
                        color = color,
                        path = stroke.toPath(
                            targetWidth = size.width,
                            targetHeight = size.height,
                        ),
                        style = Stroke(
                            width = stroke.brushSize.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
                clipRect {
                    state.strokes.forEach { stroke ->
                        drawStroke(stroke, Color.White.copy(alpha = maskAlpha))
                    }
                    activeStroke?.let { stroke ->
                        drawStroke(stroke, Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (drawEnabled) {
                        Modifier.pointerInput(state.brushSize, canvasSize) {
                            fun Offset.toCanvasPoint(): InPaintPoint {
                                val center = Offset(
                                    x = canvasSize.width / 2f,
                                    y = canvasSize.height / 2f,
                                )
                                val unscaled = Offset(
                                    x = (x - center.x - currentPan.x) / currentZoom + center.x,
                                    y = (y - center.y - currentPan.y) / currentZoom + center.y,
                                )
                                return unscaled.coerceToCanvas(canvasSize).toInPaintPoint()
                            }
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                                        activeStroke = InPaintStroke(
                                            points = listOf(offset.toCanvasPoint()),
                                            brushSize = state.brushSize,
                                            canvasWidth = canvasSize.width,
                                            canvasHeight = canvasSize.height,
                                        )
                                    }
                                },
                                onDrag = { change, _ ->
                                    activeStroke = activeStroke?.let { stroke ->
                                        stroke.copy(points = stroke.points + change.position.toCanvasPoint())
                                    }
                                },
                                onDragEnd = {
                                    activeStroke
                                        ?.takeIf { it.points.size > 1 }
                                        ?.let(onStrokeDrawn)
                                    activeStroke = null
                                },
                                onDragCancel = {
                                    activeStroke = null
                                },
                            )
                        }
                    } else if (transformEnabled) {
                        Modifier.pointerInput(canvasSize) {
                            detectTransformGestures { centroid, panChange, zoomChange, _ ->
                                val oldZoom = currentZoom
                                val newZoom = (oldZoom * zoomChange).coerceIn(
                                    MIN_IN_PAINT_ZOOM,
                                    MAX_IN_PAINT_ZOOM,
                                )
                                val actualZoomChange = newZoom / oldZoom
                                val canvasCenter = Offset(
                                    x = canvasSize.width / 2f,
                                    y = canvasSize.height / 2f,
                                )
                                val centroidFromCenter = centroid - canvasCenter
                                val newPan = if (newZoom <= MIN_IN_PAINT_ZOOM) {
                                    Offset.Zero
                                } else {
                                    currentPan * actualZoomChange +
                                            centroidFromCenter * (1f - actualZoomChange) +
                                            panChange
                                }
                                onZoomChange(newZoom)
                                onPanChange(newPan.coercePan(newZoom, canvasSize))
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

private fun Offset.coerceToCanvas(canvasSize: IntSize): Offset {
    val maxX = (canvasSize.width - 1).coerceAtLeast(0).toFloat()
    val maxY = (canvasSize.height - 1).coerceAtLeast(0).toFloat()
    return copy(
        x = x.coerceIn(0f, maxX),
        y = y.coerceIn(0f, maxY),
    )
}

private fun Offset.coercePan(zoom: Float, canvasSize: IntSize): Offset {
    if (canvasSize.width <= 0 || canvasSize.height <= 0 || zoom <= MIN_IN_PAINT_ZOOM) return Offset.Zero
    val maxX = canvasSize.width * (zoom - MIN_IN_PAINT_ZOOM) / 2f
    val maxY = canvasSize.height * (zoom - MIN_IN_PAINT_ZOOM) / 2f
    return Offset(
        x = x.coerceIn(-maxX, maxX),
        y = y.coerceIn(-maxY, maxY),
    )
}

internal const val MIN_IN_PAINT_ZOOM = 1f
internal const val MAX_IN_PAINT_ZOOM = 5f
