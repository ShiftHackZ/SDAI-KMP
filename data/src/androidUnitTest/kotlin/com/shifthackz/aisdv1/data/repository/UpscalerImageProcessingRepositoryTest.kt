package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.core.imageprocessing.ImageDownscaler
import com.shifthackz.aisdv1.data.image.BackupImageDecoder
import com.shifthackz.aisdv1.domain.entity.ProcessedImage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.io.encoding.Base64

/**
 * Checks authoritative dimensions, byte preservation and native downscale failure boundaries.
 *
 * @author Dmitriy Moroz
 */
class UpscalerImageProcessingRepositoryTest {
    private val scaler = mockk<ImageDownscaler>()
    private val decoder = mockk<BackupImageDecoder>()
    private val repository = ImageProcessingRepositoryImpl(scaler, decoder)
    private val bytes = byteArrayOf(1, 2, 3)
    private val encoded = Base64.encode(bytes)

    @Test
    fun `given standalone image repository, original headers exceed runtime bounds, expected original bytes and dimensions retained`() {
        // Given
        every { decoder.dimensions(any()) } returns (2048L to 1536L)
        // When
        val result = repository.read(encoded)
        // Then
        assertEquals(ProcessedImage(encoded, 2048, 1536), result)
        verify(exactly = 0) { scaler.downscale(any(), any(), any()) }
    }

    @Test
    fun `given standalone image repository, native preparation returns unchanged bytes, expected exact encoded source preserved`() =
        runTest {
            // Given
            every { scaler.downscale(any(), 1024, 1024) } answers { firstArg() }
            every { decoder.dimensions(any()) } returns (512L to 256L)
            // When
            val result = repository.downscale(encoded, 1024, 1024)
            // Then
            assertEquals(ProcessedImage(encoded, 512, 256), result)
        }

    @Test
    fun `given standalone image repository, proportional preparation returns new bytes, expected encoded output and actual dimensions`() =
        runTest {
            // Given
            val prepared = byteArrayOf(4, 5, 6)
            every { scaler.downscale(any(), 1024, 1024) } returns prepared
            every { decoder.dimensions(prepared) } returns (1024L to 768L)
            // When
            val result = repository.downscale(encoded, 1024, 1024)
            // Then
            assertEquals(ProcessedImage(Base64.encode(prepared), 1024, 768), result)
        }

    @Test
    fun `given standalone image repository, prepared headers exceed the requested bounds, expected rejection of unsafe output`() =
        runTest {
            // Given
            every { scaler.downscale(any(), 1024, 1024) } returns bytes
            every { decoder.dimensions(any()) } returns (1025L to 768L)
            // When
            val failure =
                runCatching { repository.downscale(encoded, 1024, 1024) }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
        }

    @Test
    fun `given standalone image repository, original header is invalid, expected rejection before inference preparation`() {
        // Given
        every { decoder.dimensions(any()) } returns (0L to 1536L)
        // When
        val failure = runCatching { repository.read(encoded) }.exceptionOrNull()
        // Then
        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun `given standalone image repository, native scaling is cancelled, expected original cancellation propagated`() =
        runTest {
            // Given
            val failure = CancellationException("native cancellation")
            every { scaler.downscale(any(), any(), any()) } throws failure
            // When
            val result = runCatching { repository.downscale(encoded, 1024, 1024) }
            // Then
            assertSame(failure, result.exceptionOrNull())
            verify(exactly = 0) { decoder.dimensions(any()) }
        }
}
