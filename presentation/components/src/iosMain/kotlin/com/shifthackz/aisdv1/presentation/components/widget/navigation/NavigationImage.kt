@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode
import kotlin.math.roundToInt

/**
 * Rasterizes a shared vector once per icon, density and layout direction for UIKit template tinting.
 * The native image retains a 24pt logical size with display-density pixels and mirrored RTL paths.
 *
 * @param icon Shared navigation vector used by both platform implementations.
 * @return Native template image, or null if the system cannot decode the encoded vector raster.
 * @author Dmitriy Moroz
 */
@Composable
internal fun rememberNavigationImage(icon: ImageVector): UIImage? {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val painter = rememberVectorPainter(icon)
    return remember(icon, density, direction) {
        val pixels = (24 * density.density).roundToInt().coerceAtLeast(1)
        val bitmap = ImageBitmap(pixels, pixels)
        CanvasDrawScope().draw(
            density,
            direction,
            Canvas(bitmap),
            Size(pixels.toFloat(), pixels.toFloat())
        ) {
            with(painter) { draw(size, colorFilter = ColorFilter.tint(Color.Black)) }
        }
        val image = Image.makeFromBitmap(bitmap.asSkiaBitmap())
        val bytes = try {
            val data = image.encodeToData(EncodedImageFormat.PNG) ?: return@remember null
            try {
                data.bytes
            } finally {
                data.close()
            }
        } finally {
            image.close()
        }
        val data = bytes.usePinned {
            NSData.create(
                bytes = it.addressOf(0),
                length = bytes.size.toULong()
            )
        }
        UIImage.imageWithData(data, scale = density.density.toDouble())
            ?.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
    }
}
