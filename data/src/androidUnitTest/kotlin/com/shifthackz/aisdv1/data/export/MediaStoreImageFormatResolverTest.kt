package com.shifthackz.aisdv1.data.export

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Exercises encoded signatures and rejection boundaries without relying on a filename or data URI.
 *
 * @author Dmitriy Moroz
 */
class MediaStoreImageFormatResolverTest {

    private val resolver = MediaStoreImageFormatResolverImpl()

    @Test
    fun `supported images resolve to the expected suffix and MIME without changing bytes`() {
        for (fixture in MediaStoreImageFixture.entries) {
            val bytes = fixture.bytes()
            val original = bytes.copyOf()
            val format = resolver.resolve(bytes)
            assertEquals(fixture.extension, format.extension)
            assertEquals(fixture.mimeType, format.mimeType)
            assertArrayEquals(original, bytes)
        }
    }

    @Test
    fun `empty or unsupported content is rejected`() {
        for (bytes in listOf(
            byteArrayOf(),
            "GIF89a".toByteArray(),
            "<html>error</html>".toByteArray()
        )) {
            assertThrows(IllegalArgumentException::class.java) { resolver.resolve(bytes) }
        }
    }

    @Test
    fun `incomplete signatures are rejected`() {
        for ((fixture, length) in listOf(
            MediaStoreImageFixture.PNG to 8,
            MediaStoreImageFixture.JPEG to 3,
            MediaStoreImageFixture.WEBP to 12,
        )) {
            for (size in 0 until length) {
                assertThrows("${fixture.name}: $size bytes", IllegalArgumentException::class.java) {
                    resolver.resolve(fixture.bytes().copyOf(size))
                }
            }
        }
    }

    @Test
    fun `each required marker byte is checked including both WebP container markers`() {
        for ((fixture, offsets) in listOf(
            MediaStoreImageFixture.PNG to (0..7).toList(),
            MediaStoreImageFixture.JPEG to (0..2).toList(),
            MediaStoreImageFixture.WEBP to ((0..3).toList() + (8..11).toList()),
        )) {
            for (offset in offsets) {
                val bytes =
                    fixture.bytes().apply { this[offset] = (this[offset].toInt() xor 1).toByte() }
                assertThrows(
                    "${fixture.name}: offset $offset",
                    IllegalArgumentException::class.java
                ) {
                    resolver.resolve(bytes)
                }
            }
        }
    }
}
