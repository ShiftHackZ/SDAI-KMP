package com.shifthackz.aisdv1.data.image

import com.shifthackz.aisdv1.data.export.MediaStoreImageFixture
import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.nio.ByteBuffer
import java.util.zip.CRC32

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlatformBackupImageDecoderTest {
    private val decoder = PlatformBackupImageDecoder()
    private val validator = BackupImageValidatorImpl(
        decoder,
        BackupImageBudget(24 * 1024 * 1024, 16_777_216, 128L * 1024 * 1024, 256)
    )

    @Test
    fun `given valid application image formats, bounded native validation runs, expected standard PNG JPEG WebP admitted`() {
        // Given
        val images = MediaStoreImageFixture.entries.map { it.bytes() }
        // When
        images.forEach(validator::invoke)
        // Then
        images.forEach { assertEquals(1L to 1L, decoder.dimensions(it)) }
    }

    @Test
    fun `given compact PNG with enormous header dimensions, native bounds probe runs, expected rejection before raster allocation`() {
        // Given
        val encoded = MediaStoreImageFixture.PNG.bytes()
        ByteBuffer.wrap(encoded).apply { putInt(16, 16384); putInt(20, 16384) }
        val crc = CRC32().apply { update(encoded, 12, 17) }.value.toInt()
        ByteBuffer.wrap(encoded).putInt(29, crc)
        // When
        val failure = assertThrows(IllegalArgumentException::class.java) { validator(encoded) }
        // Then
        assertEquals(16384L to 16384L, decoder.dimensions(encoded))
        assertEquals("Backup image exceeds pixel budget", failure.message)
    }

    @Test
    fun `given corrupted encoded image, native validation runs, expected rejection without bitmap retention`() {
        // Given
        val encoded = MediaStoreImageFixture.PNG.bytes().copyOf(20)
        // When
        assertThrows(IllegalArgumentException::class.java) { validator(encoded) }
        // Then
        assertThrows(IllegalArgumentException::class.java) { validator("corrupted image".toByteArray()) }
    }
}
