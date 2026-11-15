package com.shifthackz.aisdv1.domain.usecase.gallery

import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllGalleryIdsUseCaseImplTest {

    private val stubRepository = mockk<GenerationResultRepository>()
    private val useCase = GetAllGalleryIdsUseCaseImpl(stubRepository)

    @Test
    fun `given repository returned ids of generations, expected valid id list value`() = runTest {
        coEvery {
            stubRepository.getAllIds()
        } returns listOf(5598L, 1504L)

        val actual = useCase()

        assertEquals(listOf(5598L, 1504L), actual)
    }
}
