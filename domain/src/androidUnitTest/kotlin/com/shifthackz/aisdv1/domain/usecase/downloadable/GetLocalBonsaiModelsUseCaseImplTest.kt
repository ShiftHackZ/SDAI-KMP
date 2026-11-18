package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.mocks.mockLocalBonsaiModels
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.LocalModelCatalogResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies Bonsai list compatibility delegates strict refresh results without inventing cached success.
 *
 * @author Dmitriy Moroz
 */
class GetLocalBonsaiModelsUseCaseImplTest {

    private val stubRepository = mockk<RefreshLocalModelCatalogUseCase>()

    private val useCase = GetLocalBonsaiModelsUseCaseImpl(stubRepository)

    @Test
    fun `given Bonsai local model lookup, shared refresh returns models, expected unchanged model list`() =
        runTest {
            // Given
            coEvery {
                stubRepository(LocalAiModel.Type.Bonsai)
            } returns LocalModelCatalogResult(mockLocalBonsaiModels)

            // When
            val actual = useCase()

            // Then
            assertEquals(mockLocalBonsaiModels, actual)
        }

    @Test
    fun `given Bonsai local model lookup, shared refresh returns no models, expected empty list`() =
        runTest {
            // Given
            coEvery {
                stubRepository(LocalAiModel.Type.Bonsai)
            } returns LocalModelCatalogResult(emptyList())

            // When
            val actual = useCase()

            // Then
            assertTrue(actual.isEmpty())
        }

    @Test
    fun `given Bonsai local model lookup, shared refresh fails, expected original failure`() =
        runTest {
            // Given
            val stubException = Throwable("Unable to collect Bonsai models.")
            coEvery {
                stubRepository(LocalAiModel.Type.Bonsai)
            } throws stubException

            // When
            val actual = runCatching { useCase() }.exceptionOrNull()

            // Then
            assertSame(stubException, actual)
        }
}
