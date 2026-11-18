package com.shifthackz.aisdv1.presentation.generation.modal.history

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.generation.mocks.toGalleryMetadata
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import kotlinx.coroutines.flow.flow
import io.mockk.every
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class InputHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchersProvider = object : DispatchersProvider {
        override val io: CoroutineDispatcher = testDispatcher
        override val ui: CoroutineDispatcher = testDispatcher
        override val immediate: CoroutineDispatcher = testDispatcher
    }
    private val getGenerationResultPagedUseCase = mockk<GetGenerationResultPagedUseCase>()
    private val galleryReads = mockk<GalleryReadGateway>()

    @Before
    fun initialize() {
        Localization.setLanguageCode("en")
        every { galleryReads.observeMetadata(any(), any()) } answers {
            val limit = firstArg<Int>()
            val offset = secondArg<Int>()
            flow {
                emit(
                    getGenerationResultPagedUseCase(
                        limit,
                        offset
                    ).map { it.toGalleryMetadata() })
            }
        }
        coEvery {
            getGenerationResultPagedUseCase(limit = PAGE_SIZE, offset = 0)
        } returns listOf(generationResult(id = 1L))
        coEvery {
            getGenerationResultPagedUseCase(limit = PAGE_SIZE, offset = PAGE_SIZE)
        } returns emptyList()
    }

    @Test
    fun `given input history, initialized, expected first page loaded`() =
        runTest(testDispatcher) {
            // Given
            // initialize() stubs the first page with generation result ID 1 and the existing PAGE_SIZE
            // limit.

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                InputHistoryState(
                    loading = false,
                    items = listOf(
                        InputHistoryItemUi(generationResult = generationResult(id = 1L).toGalleryMetadata()),
                    ),
                    nextPage = 1,
                    canLoadMore = true,
                ),
                viewModel.state.value,
            )
            coVerify {
                getGenerationResultPagedUseCase(limit = PAGE_SIZE, offset = 0)
            }

        }

    @Test
    fun `given input history, load next page intent, expected next page appended`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                getGenerationResultPagedUseCase(limit = PAGE_SIZE, offset = PAGE_SIZE)
            } returns listOf(generationResult(id = 2L))
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.processIntent(InputHistoryIntent.LoadNextPage)
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                InputHistoryState(
                    loading = false,
                    items = listOf(
                        InputHistoryItemUi(generationResult = generationResult(id = 1L).toGalleryMetadata()),
                        InputHistoryItemUi(generationResult = generationResult(id = 2L).toGalleryMetadata()),
                    ),
                    nextPage = 2,
                    canLoadMore = true,
                ),
                viewModel.state.value,
            )
            coVerify {
                getGenerationResultPagedUseCase(limit = PAGE_SIZE, offset = PAGE_SIZE)
            }

        }

    @Test
    fun `given input history, empty next page, expected loading disabled`() =
        runTest(testDispatcher) {
            // Given

            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.processIntent(InputHistoryIntent.LoadNextPage)
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                InputHistoryState(
                    loading = false,
                    items = listOf(
                        InputHistoryItemUi(generationResult = generationResult(id = 1L).toGalleryMetadata()),
                    ),
                    nextPage = 2,
                    canLoadMore = false,
                ),
                viewModel.state.value,
            )

        }

    @Test
    fun `given input history, initial load failed and retry received, expected data reloaded`() =
        runTest(testDispatcher) {
            // Given

            coEvery {
                getGenerationResultPagedUseCase(limit = PAGE_SIZE, offset = 0)
            } throws IllegalStateException()

            val viewModel = createViewModel()
            advanceUntilIdle()

            Assert.assertEquals(Localization.string("error_generic"), viewModel.state.value.error)

            coEvery {
                getGenerationResultPagedUseCase(limit = PAGE_SIZE, offset = 0)
            } returns listOf(generationResult(id = 1L))

            // When
            viewModel.processIntent(InputHistoryIntent.Retry)
            advanceUntilIdle()

            // Then
            Assert.assertEquals(
                InputHistoryState(
                    loading = false,
                    items = listOf(
                        InputHistoryItemUi(generationResult = generationResult(id = 1L).toGalleryMetadata()),
                    ),
                    nextPage = 1,
                    canLoadMore = true,
                ),
                viewModel.state.value,
            )

        }

    private fun TestScope.createViewModel() = InputHistoryViewModel(
        diagnostics = mockk(relaxed = true),
        dispatchersProvider = dispatchersProvider,
        galleryReads = galleryReads,
        getGenerationResultUseCase = mockk(relaxed = true),
    )

    private fun generationResult(id: Long) = AiGenerationResult(
        id = id,
        image = "",
        inputImage = "",
        createdAt = 0L,
        type = AiGenerationResult.Type.TEXT_TO_IMAGE,
        prompt = "Prompt $id",
        negativePrompt = "",
        width = 512,
        height = 512,
        samplingSteps = 20,
        cfgScale = 7f,
        restoreFaces = false,
        sampler = "Euler",
        seed = "seed-$id",
        subSeed = "",
        subSeedStrength = 0f,
        denoisingStrength = 0f,
        hidden = false,
    )

    private companion object {
        const val PAGE_SIZE = 60
    }
}
