package com.shifthackz.aisdv1.app.inference

import android.app.Application
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards Bonsai output publication against truncated, corrupted and wrong-size PNGs.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class, sdk = [28])
class CompletePngImageValidatorTest {
    private val validator = CompletePngImageValidator()
    private val request = bonsaiRequest()

    @Test
    fun `given complete PNG matching Bonsai dimensions, validation succeeds`() {
        validator.validate(png(), request)
    }

    @Test
    fun `given truncated PNG, output is rejected`() {
        val image = png()

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                image.copyOf(image.size - 12),
                request
            )
        }
    }

    @Test
    fun `given corrupt chunk CRC, output is rejected`() {
        val image = png().apply { this[29] = (this[29].toInt() xor 1).toByte() }

        assertThrows(IllegalArgumentException::class.java) { validator.validate(image, request) }
    }

    @Test
    fun `given different output dimensions, Bonsai output is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                png(),
                request.copy(payload = request.payload.copy(width = 256))
            )
        }
    }

    @Test
    fun `given trailing bytes after IEND, output is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                png() + byteArrayOf(
                    1
                ), request
            )
        }
    }

    private fun png(): ByteArray = ByteArrayOutputStream().use {
        val bitmap = Bitmap.createBitmap(128, 256, Bitmap.Config.ARGB_8888)
        try {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            it.toByteArray()
        } finally {
            bitmap.recycle()
        }
    }
}
