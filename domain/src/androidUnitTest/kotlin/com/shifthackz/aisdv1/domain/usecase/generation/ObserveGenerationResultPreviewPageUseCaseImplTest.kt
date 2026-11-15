package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResultPreviews
import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveGenerationResultPreviewPageUseCaseImplTest {

    private val stubRepository = mockk<GenerationResultRepository>()
    private val useCase = ObserveGenerationResultPreviewPageUseCaseImpl(stubRepository)

    @Test
    fun `given repository observes preview page, expected valid preview list value`() = runTest {
        every {
            stubRepository.observePagePreview(any(), any())
        } returns flowOf(mockAiGenerationResultPreviews)

        val actual = useCase(30, 0).first()

        assertEquals(mockAiGenerationResultPreviews, actual)
    }
}
