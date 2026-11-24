package com.shifthackz.aisdv1.presentation.faq

import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqContent
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import com.shifthackz.aisdv1.domain.usecase.faq.FetchOrGetFaqUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects FAQ load transitions, retry serialization, and route cancellation.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FaqViewModelTest {
    private val store = ViewModelStore()
    private val load = mockk<FetchOrGetFaqUseCase>()
    private val online = FaqContent(
        categories = listOf(
            FaqCategory(
                "setup",
                "Setup",
                listOf(FaqQuestion("connect", "How?", listOf("Open Settings."), emptyList()))
            ),
        ),
        isCached = false,
    )

    @After
    fun restoreMainDispatcher() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `given FAQ view model with initial request, screen loads, expected loading then online answers`() =
        runTest {
            // Given
            val pending = CompletableDeferred<FaqContent>()
            coEvery { load() } coAnswers { Result.success(pending.await()) }
            val model = createViewModel()
            assertEquals(FaqState.Loading, model.state.value)

            // When
            pending.complete(online)
            advanceUntilIdle()

            // Then
            assertEquals(FaqState.Ready(online), model.state.value)
            coVerify(exactly = 1) { load() }
        }

    @Test
    fun `given FAQ view model with saved answers, cached result is loaded, expected cached flag is retained`() =
        runTest {
            // Given
            val cached = online.copy(isCached = true)
            coEvery { load() } returns Result.success(cached)

            // When
            val model = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(FaqState.Ready(cached), model.state.value)
        }

    @Test
    fun `given FAQ view model with failed load, retry succeeds, expected error loading and ready transitions`() =
        runTest {
            // Given
            coEvery { load() } returns Result.failure(IllegalStateException("Offline"))
            val model = createViewModel()
            advanceUntilIdle()
            assertEquals(FaqState.Error, model.state.value)
            coEvery { load() } returns Result.success(online)

            // When
            model.processIntent(FaqIntent.Retry)
            val retryState = model.state.value
            advanceUntilIdle()

            // Then
            assertEquals(FaqState.Loading, retryState)
            assertEquals(FaqState.Ready(online), model.state.value)
            coVerify(exactly = 2) { load() }
        }

    @Test
    fun `given FAQ view model with active retry, another retry is requested, expected just one in-flight fetch`() =
        runTest {
            // Given
            coEvery { load() } returns Result.success(online.copy(isCached = true))
            val model = createViewModel()
            advanceUntilIdle()
            val pending = CompletableDeferred<FaqContent>()
            coEvery { load() } coAnswers { Result.success(pending.await()) }

            // When
            model.processIntent(FaqIntent.Retry)
            runCurrent()
            repeat(3) { model.processIntent(FaqIntent.Retry) }
            pending.complete(online)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 2) { load() }
            assertEquals(FaqState.Ready(online), model.state.value)
        }

    @Test
    fun `given FAQ view model with active request, route is disposed, expected request cancels before publishing error or ready`() =
        runTest {
            // Given
            var wasCancelled = false
            coEvery { load() } coAnswers {
                try {
                    awaitCancellation()
                } finally {
                    wasCancelled = true
                }
            }
            val model = createViewModel()
            runCurrent()

            // When
            store.clear()
            advanceUntilIdle()

            // Then
            assertTrue(wasCancelled)
            assertEquals(FaqState.Loading, model.state.value)
        }

    private fun TestScope.createViewModel(): FaqViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val dispatchers = mockk<DispatchersProvider> { every { immediate } returns dispatcher }
        return FaqViewModel(mockk<UiDiagnostics>(relaxed = true), dispatchers, load).also {
            store.put("faq", it)
        }
    }
}
