package com.shifthackz.aisdv1.feature.backup.restore

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class BackupImageFingerprintTest {
    private val fingerprint = DefaultBackupImageFingerprint()

    @Test
    fun `given same encoded image, Base64 alphabet whitespace padding and chunk sizes differ, expected identical fingerprint`() =
        runTest {
            // Given
            val text = Base64.encode(byteArrayOf(1, 2, 3, -5, -1))
            val variant = text.trimEnd('=').replace('+', '-').replace('/', '_').chunked(2)
                .joinToString(" \n\u2003")

            // When
            val expected = identify(text, 65536)
            val actual = identify(variant, 1)

            // Then
            assertTrue(expected != null)
            assertEquals(expected, actual)
        }

    @Test
    fun `given image content changes by one byte, fingerprints are read in bounded chunks, expected different identity`() =
        runTest {
            // Given
            val first = Base64.encode(ByteArray(200000) { (it % 251).toByte() })
            val second = Base64.encode(ByteArray(200000) { ((it + 1) % 251).toByte() })

            // When
            val firstHash = identify(first, 65536)
            val secondHash = identify(second, 7)

            // Then
            assertNotEquals(firstHash, secondHash)
        }

    @Test
    fun `given malformed saved Base64, padding appears before trailing content, expected no identity`() =
        runTest {
            // Given
            val text = "AQ==AQ=="

            // When
            val identity = identify(text, 3)

            // Then
            assertNull(identity)
        }

    @Test
    fun `given saved image is absent or empty, duplicate scan requests its content, expected no identity`() =
        runTest {
            // Given
            val empty = " \n\t"

            // When
            val missing = fingerprint { _, _ -> null }
            val blank = identify(empty, 1)

            // Then
            assertNull(missing)
            assertNull(blank)
        }

    @Test
    fun `given duplicate scan source fails, content reader raises storage error, expected failure propagated`() =
        runTest {
            // Given
            val failure = IllegalStateException("storage unavailable")
            var caught: Throwable? = null

            // When
            try {
                fingerprint { _, _ -> throw failure }
            } catch (error: IllegalStateException) {
                caught = error
            }

            // Then
            assertEquals(failure, caught)
        }

    @Test
    fun `given duplicate scan is canceled, reader propagates cancellation, expected cancellation retained`() =
        runTest {
            // Given
            val failure = CancellationException("cancel")
            var caught: Throwable? = null

            // When
            try {
                fingerprint { _, _ -> throw failure }
            } catch (error: CancellationException) {
                caught = error
            }

            // Then
            assertEquals(failure, caught)
        }

    /**
     * Feeds text at controlled boundaries to exercise Base64 groups and multibyte UTF8 whitespace split across reads.
     * @param text Encoded image text, including supported presentation whitespace.
     * @param chunkSize Maximum UTF8 bytes returned per read, never exceeding the production request.
     * @author Dmitriy Moroz
     */
    private suspend fun identify(text: String, chunkSize: Int): String? {
        val bytes = text.encodeToByteArray()
        return fingerprint { offset, maxBytes ->
            if (offset >= bytes.size) byteArrayOf() else bytes.copyOfRange(
                offset.toInt(),
                minOf(offset + minOf(chunkSize, maxBytes), bytes.size.toLong()).toInt()
            )
        }
    }
}
