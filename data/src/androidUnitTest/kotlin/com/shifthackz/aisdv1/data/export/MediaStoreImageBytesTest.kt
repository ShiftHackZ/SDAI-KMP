package com.shifthackz.aisdv1.data.export

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Base64

/**
 * Guards against bitmap recompression by comparing complete metadata-bearing files byte for byte.
 *
 * @param fixture Encoded image format exercised by each Base64 transport variant.
 * @author Dmitriy Moroz
 */
@RunWith(Parameterized::class)
class MediaStoreImageBytesTest(private val fixture: MediaStoreImageFixture) {

    @Test
    fun `raw Base64 preserves the complete image and metadata`() {
        val bytes = fixture.bytes()
        assertTrue(bytes.toString(Charsets.ISO_8859_1).contains("Seed: 42"))
        assertArrayEquals(bytes, Base64.getEncoder().encodeToString(bytes).toMediaStoreImageBytes())
    }

    @Test
    fun `data URI header does not override the actual encoded format`() {
        val bytes = fixture.bytes()
        val encoded = "data:image/jpeg;base64,${Base64.getEncoder().encodeToString(bytes)}"
        assertArrayEquals(bytes, encoded.toMediaStoreImageBytes())
    }

    @Test
    fun `wrapped Base64 with spaces tabs and newlines preserves the file`() {
        val bytes = fixture.bytes()
        val encoded = Base64.getEncoder().encodeToString(bytes).chunked(17).joinToString(" \t\r\n")
        assertArrayEquals(bytes, encoded.toMediaStoreImageBytes())
    }

    @Test
    fun `optional trailing padding is accepted for all remainder lengths`() {
        val image = fixture.bytes()
        for (extra in 0..2) {
            val bytes = image + ByteArray(extra)
            val encoded = Base64.getEncoder().withoutPadding().encodeToString(bytes)
            assertArrayEquals(bytes, encoded.toMediaStoreImageBytes())
            assertArrayEquals(
                bytes,
                "data:${fixture.mimeType};base64,$encoded".toMediaStoreImageBytes()
            )
        }
    }

    @Test
    fun `malformed Base64 fails rather than returning partial image bytes`() {
        val encoded = Base64.getEncoder().encodeToString(fixture.bytes())
        for (invalid in listOf(
            "$encoded!",
            "A",
            "AA=A",
            "AA===",
            "data:${fixture.mimeType};base64,!"
        )) {
            assertThrows(invalid, IllegalArgumentException::class.java) {
                invalid.toMediaStoreImageBytes()
            }
        }
    }

    companion object {
        /**
         * Repeats the export contract for PNG, JPEG and WebP fixtures.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun formats(): Array<MediaStoreImageFixture> = MediaStoreImageFixture.entries.toTypedArray()
    }
}
