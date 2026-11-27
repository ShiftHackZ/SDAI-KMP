package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ProcessedImage
import com.shifthackz.aisdv1.domain.repository.ImageProcessingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Keeps original image admission independent of later downscale confirmation and propagates failures.
 *
 * @author Dmitriy Moroz
 */
class ReadUpscalerImageUseCaseImplTest {
    private val repository = mockk<ImageProcessingRepository>()
    private val useCase = ReadUpscalerImageUseCaseImpl(repository)

    @Test
    fun `given standalone image admission, oversized portrait is read, expected original bytes and authoritative dimensions`() {
        // Given
        val original = ProcessedImage("original", 1536, 2048)
        every { repository.read("original") } returns original
        // When
        val result = useCase("original")
        // Then
        assertSame(original, result)
        verify(exactly = 1) { repository.read("original") }
    }

    @Test
    fun `given standalone image admission, header decoding fails, expected original failure`() {
        // Given
        val failure = IllegalArgumentException("invalid image")
        every { repository.read(any()) } throws failure
        // When
        val result = runCatching { useCase("invalid") }
        // Then
        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun `given standalone image admission, repository is cancelled, expected original cancellation`() {
        // Given
        val failure = CancellationException("cancel admission")
        every { repository.read(any()) } throws failure
        // When
        val result = runCatching { useCase("original") }
        // Then
        assertSame(failure, result.exceptionOrNull())
    }
}
