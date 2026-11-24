package com.shifthackz.aisdv1.core.imageprocessing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Checks real Android PNG preparation preserves the whole composition and aspect ratio before upscaling.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UpscalerImageDownscalerTest {
    private val scaler = PlatformImageDownscaler()

    @Test
    fun `given landscape upscaler input, longest edge exceeds 1024, expected proportional whole image without center crop`() {
        // Given
        val source = image(2048, 1536)
        // When
        val prepared = scaler.downscale(source, 1024, 1024)
        val decoded = requireNotNull(BitmapFactory.decodeByteArray(prepared, 0, prepared.size))
        // Then
        try {
            assertEquals(1024, decoded.width)
            assertEquals(768, decoded.height)
            assertQuadrants(decoded)
        } finally {
            decoded.recycle()
        }
    }

    @Test
    fun `given portrait upscaler input, longest edge exceeds 1024, expected proportional whole image without center crop`() {
        // Given
        val source = image(1536, 2048)
        // When
        val prepared = scaler.downscale(source, 1024, 1024)
        val decoded = requireNotNull(BitmapFactory.decodeByteArray(prepared, 0, prepared.size))
        // Then
        try {
            assertEquals(768, decoded.width)
            assertEquals(1024, decoded.height)
            assertQuadrants(decoded)
        } finally {
            decoded.recycle()
        }
    }

    @Test
    fun `given small upscaler input, source already fits both bounds, expected byte identity without reencoding`() {
        // Given
        val source = image(128, 96)
        // When
        val prepared = scaler.downscale(source, 1024, 1024)
        // Then
        assertSame(source, prepared)
    }

    @Test
    fun `given invalid upscaler input, encoded pixels cannot be decoded, expected rejection`() {
        // Given
        val source = byteArrayOf(1, 2, 3)
        // When
        val failure = runCatching { scaler.downscale(source, 1024, 1024) }.exceptionOrNull()
        // Then
        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun `given non-square upscaler dimensions, integer fitting is calculated, expected exact longest edge and floor-rounded shorter edge`() {
        // Given
        val width = 3001
        val height = 1001
        // When
        val fitted = downscaledImageDimensions(width, height, 1024, 1024)
        // Then
        assertEquals(1024 to 341, fitted)
    }

    @Test
    fun `given extreme upscaler dimensions, integer products exceed 32 bits, expected bounded result without overflow`() {
        // Given
        val width = Int.MAX_VALUE
        // When
        val fitted = downscaledImageDimensions(width, 1, 1024, 1024)
        // Then
        assertEquals(1024 to 1, fitted)
    }

    /**
     * Creates an encoded source with distinct quadrants so cropping loses observable composition.
     *
     * @param width Original pixel width.
     * @param height Original pixel height.
     * @author Dmitriy Moroz
     */
    private fun image(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.color = Color.RED
            canvas.drawRect(0f, 0f, width / 2f, height / 2f, paint)
            paint.color = Color.BLUE
            canvas.drawRect(width / 2f, 0f, width.toFloat(), height / 2f, paint)
            paint.color = Color.GREEN
            canvas.drawRect(0f, height / 2f, width / 2f, height.toFloat(), paint)
            paint.color = Color.YELLOW
            canvas.drawRect(width / 2f, height / 2f, width.toFloat(), height.toFloat(), paint)
            return ByteArrayOutputStream().use {
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                it.toByteArray()
            }
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Checks all four original regions survive proportional resampling in the same orientation.
     *
     * @param bitmap Downscaled native raster.
     * @author Dmitriy Moroz
     */
    private fun assertQuadrants(bitmap: Bitmap) {
        assertEquals(Color.RED, bitmap.getPixel(bitmap.width / 4, bitmap.height / 4))
        assertEquals(Color.BLUE, bitmap.getPixel(bitmap.width * 3 / 4, bitmap.height / 4))
        assertEquals(Color.GREEN, bitmap.getPixel(bitmap.width / 4, bitmap.height * 3 / 4))
        assertEquals(Color.YELLOW, bitmap.getPixel(bitmap.width * 3 / 4, bitmap.height * 3 / 4))
    }
}
