package com.shifthackz.aisdv1.presentation.components.widget.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs

/**
 * Decoded image sources accepted by the bounded zoom/pan renderer.
 *
 * @author Dmitriy Moroz
 */
sealed interface ZoomableImageSource {
    /**
     * Already decoded pixels; this renderer performs no Base64 decoding or image loading.
     *
     * @param image Bitmap whose pixels are displayed and transformed.
     *
     * @author Dmitriy Moroz
     */
    data class Bitmap(val image: ImageBitmap) : ZoomableImageSource
}

/**
 * Displays decoded pixels with bounded pinch zoom and panning, resetting transforms when source changes.
 * At minimum zoom single-finger gestures remain available to the enclosing pager/scroll container.
 *
 * @param modifier Layout modifiers for the clipped image viewport.
 * @param source Already decoded bitmap supplied by an admitted image loader.
 * @param backgroundColor Color exposed around the fitted image.
 * @param minScale Lower bound applied to gesture-driven zoom.
 * @param maxScale Upper bound applied to gesture-driven zoom.
 * @param hideImage Whether a blur obscures the displayed image.
 * @param hideBlurRadius Blur radius applied while the image is obscured.
 * @param fitToWidth Whether the unscaled image width follows the viewport while preserving aspect ratio.
 * @param gesturesEnabled Whether this renderer consumes image transform gestures.
 * @param onScaleChange Reports initial and gesture-updated zoom for surrounding UI coordination.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    source: ZoomableImageSource,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    minScale: Float = 1f,
    maxScale: Float = 6f,
    hideImage: Boolean = false,
    hideBlurRadius: Float = 69f,
    fitToWidth: Boolean = false,
    gesturesEnabled: Boolean = true,
    onScaleChange: (Float) -> Unit = {},
) {
    val scale = remember(source) { mutableFloatStateOf(1f) }
    var offsetX by remember(source) { mutableFloatStateOf(0f) }
    var offsetY by remember(source) { mutableFloatStateOf(0f) }
    var containerSize by remember(source) { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(source) {
        onScaleChange(scale.floatValue)
    }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .fillMaxSize()
            .onSizeChanged { size ->
                containerSize = size
                val imageSize = source.sizeForContainer(size, fitToWidth)
                offsetX = offsetX.coerceInBounds(
                    containerSize = size.width.toFloat(),
                    contentSize = imageSize.width,
                    scale = scale.floatValue,
                )
                offsetY = offsetY.coerceInBounds(
                    containerSize = size.height.toFloat(),
                    contentSize = imageSize.height,
                    scale = scale.floatValue,
                )
            }
            .background(backgroundColor)
            .pointerInput(source, gesturesEnabled) {
                if (!gesturesEnabled) return@pointerInput
                detectZoomableImageGestures(
                    isPanningEnabled = {
                        scale.floatValue > minScale + ZOOM_EPSILON
                    },
                    onGesture = { pan, zoom ->
                        scale.floatValue = (scale.floatValue * zoom).coerceIn(minScale, maxScale)
                        onScaleChange(scale.floatValue)
                        val imageSize = source.sizeForContainer(containerSize, fitToWidth)
                        offsetX = (offsetX + pan.x * zoom).coerceInBounds(
                            containerSize = containerSize.width.toFloat(),
                            contentSize = imageSize.width,
                            scale = scale.floatValue,
                        )
                        offsetY = (offsetY + pan.y * zoom).coerceInBounds(
                            containerSize = containerSize.height.toFloat(),
                            contentSize = imageSize.height,
                            scale = scale.floatValue,
                        )
                    },
                )
            },
    ) {
        val imageModifier = Modifier
            .align(Alignment.Center)
            .then(
                if (fitToWidth) {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(source.aspectRatio)
                } else {
                    Modifier
                }
            )
            .graphicsLayer(
                scaleX = scale.floatValue,
                scaleY = scale.floatValue,
                translationX = offsetX,
                translationY = offsetY,
            )
            .then(
                if (!hideImage) Modifier
                else Modifier.graphicsLayer {
                    renderEffect = BlurEffect(
                        radiusX = hideBlurRadius,
                        radiusY = hideBlurRadius,
                    )
                }
            )

        when (source) {
            is ZoomableImageSource.Bitmap -> Image(
                modifier = imageModifier,
                contentDescription = null,
                bitmap = source.image,
                contentScale = ContentScale.Fit,
            )
        }
    }
}


private val ZoomableImageSource.aspectRatio: Float
    get() = bitmap.width.toFloat() / bitmap.height.coerceAtLeast(1)


private val ZoomableImageSource.bitmap: ImageBitmap
    get() = (this as ZoomableImageSource.Bitmap).image


private data class ImageContentSize(

    val width: Float,

    val height: Float,
)


private fun ZoomableImageSource.sizeForContainer(
    containerSize: IntSize,
    fitToWidth: Boolean,
): ImageContentSize {

    val image = bitmap


    val sourceWidth = image.width.toFloat()


    val sourceHeight = image.height.toFloat().coerceAtLeast(1f)
    if (!fitToWidth || containerSize.width <= 0) {
        return ImageContentSize(sourceWidth, sourceHeight)
    }

    val width = containerSize.width.toFloat()
    return ImageContentSize(
        width = width,
        height = width / aspectRatio,
    )
}


private fun Float.coerceInBounds(
    containerSize: Float,
    contentSize: Float,
    scale: Float,
): Float {
    val overflow = (contentSize * scale - containerSize) / 2f
    if (overflow <= 0f) return 0f
    return coerceIn(-overflow, overflow)
}

private suspend fun PointerInputScope.detectZoomableImageGestures(
    isPanningEnabled: () -> Boolean,
    onGesture: (pan: Offset, zoom: Float) -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var pastTouchSlop = false
        var accumulatedZoom = 1f
        var accumulatedPan = Offset.Zero
        val touchSlop = viewConfiguration.touchSlop

        do {
            val event = awaitPointerEvent()
            val pointerCount = event.changes.count { it.pressed }
            val zoomChange = event.calculateZoom()
            val panChange = event.calculatePan()
            val handlesGesture = pointerCount > 1 ||
                    isPanningEnabled() ||
                    abs(zoomChange - 1f) > ZOOM_EPSILON

            if (handlesGesture) {
                if (!pastTouchSlop) {
                    accumulatedZoom *= zoomChange
                    accumulatedPan += panChange
                    val zoomMotion = abs(1 - accumulatedZoom) *
                            event.calculateCentroidSize(useCurrent = false)
                    val panMotion = accumulatedPan.getDistance()
                    pastTouchSlop = zoomMotion > touchSlop || panMotion > touchSlop
                }

                if (pastTouchSlop) {
                    onGesture(panChange, zoomChange)
                    event.changes.forEach { change ->
                        if (change.positionChanged()) {
                            change.consume()
                        }
                    }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

private const val ZOOM_EPSILON = 0.01f
