package com.shifthackz.aisdv1.presentation.components.modal.history

import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies metadata paging and explicit original selection through production domain contracts.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InputHistorySelectionTest {
    private val reads = mockk<GalleryReadGateway>()
    private val original = mockk<GetGenerationResultUseCase>()
    private val errors = mutableListOf<Throwable>()
    private val stores = mutableListOf<ViewModelStore>()

    @After
    fun tearDown() {
        stores.forEach { it.clear() }
        Dispatchers.resetMain()
    }

    /** Builds actual persisted gallery metadata without retaining image payloads.
     * @param id Stable gallery identity.
     * @author Dmitriy Moroz
     */
    private fun metadata(id: Long) = GalleryItemMetadata(
        id, "revision-$id", 1L, AiGenerationResult.Type.TEXT_TO_IMAGE,
        "Landscape", "", 512, 512, 20, 7f, false, "Euler", "1", "", 0f, 0f, false, false, "SD1.5"
    )

    /** Creates the production view model under a lifecycle owner.
     * @author Dmitriy Moroz
     */
    private fun TestScope.createModel(): InputHistoryViewModel {
        Localization.setLanguageCode("en")
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val dispatchers = mockk<DispatchersProvider> {
            every { io } returns dispatcher
            every { immediate } returns dispatcher
        }
        return InputHistoryViewModel(
            mockk(relaxed = true),
            dispatchers,
            reads,
            original,
            errors::add
        )
            .also { model -> stores.add(ViewModelStore().apply { put("history", model) }) }
    }

    @Test
    fun `given input history, metadata loads, expected original image is never fetched eagerly`() =
        runTest {
            // Given
            every { reads.observeMetadata(60, 0) } returns flowOf(listOf(metadata(7)))
            // When
            val model = createModel()
            advanceUntilIdle()
            // Then
            assertEquals(listOf(7L), model.state.value.items.map { it.generationResult.id })
            coVerify(exactly = 0) { original(any()) }
        }

    @Test
    fun `given input history, next page requested while initial read is pending, expected one metadata read`() =
        runTest {
            // Given
            val gate = CompletableDeferred<Unit>()
            every {
                reads.observeMetadata(
                    60,
                    0
                )
            } returns flow { gate.await(); emit(listOf(metadata(7))) }
            val model = createModel()
            runCurrent()
            // When
            model.processIntent(InputHistoryIntent.LoadNextPage)
            gate.complete(Unit)
            advanceUntilIdle()
            // Then
            verify(exactly = 1) { reads.observeMetadata(any(), any()) }
            assertEquals(1, model.state.value.nextPage)
        }

    @Test
    fun `given paged input history, next page is requested twice while pending, expected one appended page`() =
        runTest {
            // Given
            every { reads.observeMetadata(60, 0) } returns flowOf(listOf(metadata(7)))
            val gate = CompletableDeferred<Unit>()
            every { reads.observeMetadata(60, 60) } returns flow {
                gate.await(); emit(
                listOf(
                    metadata(8)
                )
            )
            }
            val model = createModel()
            advanceUntilIdle()
            // When
            model.processIntent(InputHistoryIntent.LoadNextPage)
            model.processIntent(InputHistoryIntent.LoadNextPage)
            gate.complete(Unit)
            advanceUntilIdle()
            // Then
            verify(exactly = 1) { reads.observeMetadata(60, 60) }
            assertEquals(listOf(7L, 8L), model.state.value.items.map { it.generationResult.id })
        }

    @Test
    fun `given exhausted input history, next page is requested, expected no further read`() =
        runTest {
            // Given
            every { reads.observeMetadata(60, 0) } returns flowOf(emptyList())
            val model = createModel()
            advanceUntilIdle()
            // When
            model.processIntent(InputHistoryIntent.LoadNextPage)
            advanceUntilIdle()
            // Then
            verify(exactly = 1) { reads.observeMetadata(any(), any()) }
            assertFalse(model.state.value.canLoadMore)
        }

    @Test
    fun `given failed input history, retry succeeds, expected error replaced by fresh metadata`() =
        runTest {
            // Given
            every {
                reads.observeMetadata(
                    60,
                    0
                )
            } returns flow { throw IllegalStateException("read failed") }
            val model = createModel()
            advanceUntilIdle()
            assertTrue(model.state.value.error != null)
            every { reads.observeMetadata(60, 0) } returns flowOf(listOf(metadata(9)))
            // When
            model.processIntent(InputHistoryIntent.Retry)
            advanceUntilIdle()
            // Then
            assertEquals(null, model.state.value.error)
            assertEquals(listOf(9L), model.state.value.items.map { it.generationResult.id })
            assertEquals(1, errors.size)
        }

    @Test
    fun `given input history, selected original fails to load, expected no selection and visible error`() =
        runTest {
            // Given
            every { reads.observeMetadata(60, 0) } returns flowOf(listOf(metadata(7)))
            val failure = IllegalStateException("missing original")
            coEvery { original(7) } throws failure
            val model = createModel()
            advanceUntilIdle()
            var selected: AiGenerationResult? = null
            // When
            model.select(7) { selected = it }
            advanceUntilIdle()
            // Then
            assertEquals(null, selected)
            assertEquals(listOf(failure), errors)
            assertTrue(model.state.value.error != null)
        }

    @Test
    fun `given input history, original selection is cancelled, expected no error feedback or selected callback`() =
        runTest {
            // Given
            every { reads.observeMetadata(60, 0) } returns flowOf(emptyList())
            coEvery { original(7) } throws CancellationException()
            val model = createModel()
            advanceUntilIdle()
            var hasSelected = false
            // When
            model.select(7) { hasSelected = true }
            advanceUntilIdle()
            // Then
            assertFalse(hasSelected)
            assertEquals(emptyList<Throwable>(), errors)
            assertEquals(null, model.state.value.error)
        }

    @Test
    fun `given input history, persisted image is selected, expected unchanged original delivered exactly once`() =
        runTest {
            // Given
            every { reads.observeMetadata(60, 0) } returns flowOf(listOf(metadata(7)))
            val result = AiGenerationResult(
                7,
                "output pixels",
                "input pixels",
                1L,
                AiGenerationResult.Type.IMAGE_TO_IMAGE,
                "Original prompt",
                "Original negative",
                768,
                512,
                24,
                6f,
                false,
                "Euler",
                "42",
                "",
                0f,
                0.7f,
                false
            )
            coEvery { original(7) } returns result
            val model = createModel()
            advanceUntilIdle()
            val selections = mutableListOf<AiGenerationResult>()
            // When
            model.select(7) { selections.add(it) }
            advanceUntilIdle()
            // Then
            assertEquals(listOf(result), selections)
            coVerify(exactly = 1) { original(7) }
        }
}
