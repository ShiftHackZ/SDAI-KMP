package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.mocks.mockTextToImagePayload
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Keeps unsupported platform bindings explicit instead of producing successful empty images.
 *
 * @author Dmitriy Moroz
 */
class UnsupportedMnnGenerationRepositoryTest {
    @Test
    fun `given unsupported MNN repository, setup validation is requested, expected explicit platform failure`() =
        runTest {
            // Given
            val repository = UnsupportedMnnGenerationRepository

            // When
            val failure =
                runCatching { repository.validateModel("mnn_model", "/external") }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
        }

    @Test
    fun `given unsupported MNN repository, owned model selection is requested, expected explicit platform failure`() =
        runTest {
            // Given
            val repository = UnsupportedMnnGenerationRepository

            // When
            val failure = runCatching {
                repository.validateOwnedModel(
                    "mnn_model",
                    "a".repeat(64)
                )
            }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
        }

    @Test
    fun `given unsupported MNN repository, generation is requested, expected failure without output`() =
        runTest {
            // Given
            val payload = mockTextToImagePayload

            // When
            val failure =
                runCatching { UnsupportedMnnGenerationRepository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
        }

    @Test
    fun `given unsupported MNN repository, progress and interruption are requested, expected empty stream and harmless interrupt`() =
        runTest {
            // Given
            val repository = UnsupportedMnnGenerationRepository

            // When
            val statuses = repository.observeStatus().toList()
            repository.interruptGeneration()

            // Then
            assertEquals(emptyList<Any>(), statuses)
        }
}
