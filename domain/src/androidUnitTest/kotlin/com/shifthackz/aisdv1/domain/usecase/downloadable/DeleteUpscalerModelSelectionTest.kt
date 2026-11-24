package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects the independent upscaler selection from failed or unrelated model removals.
 *
 * @author Dmitriy Moroz
 */
class DeleteUpscalerModelSelectionTest {
    private val repository = mockk<DownloadableModelRepository>()
    private val preferences = mockk<PreferenceManager>(relaxed = true)
    private val useCase = DeleteModelUseCaseImpl(repository, preferences)

    @Test
    fun `given selected upscaler removal, owned payload is removed, expected independent selection cleared`() =
        runTest {
            // Given
            every { preferences.upscalerModelId } returns "upscaler_test"
            coEvery { repository.delete("upscaler_test") } returns Unit

            // When
            useCase("upscaler_test")

            // Then
            verify(exactly = 1) { preferences.upscalerModelId = "" }
            verify(exactly = 0) { preferences.localMnnModelId = any() }
        }

    @Test
    fun `given selected upscaler removal, repository fails, expected selection preserved and failure propagated`() =
        runTest {
            // Given
            every { preferences.upscalerModelId } returns "upscaler_test"
            coEvery { repository.delete("upscaler_test") } throws IllegalStateException("removal failed")

            // When
            val failure = runCatching { useCase("upscaler_test") }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            verify(exactly = 0) { preferences.upscalerModelId = any() }
        }
}
