package com.shifthackz.aisdv1.presentation.blog.list

import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.domain.entity.BlogContent
import com.shifthackz.aisdv1.domain.usecase.blog.FetchOrGetBlogUseCase
import com.shifthackz.aisdv1.presentation.blog.releasePost
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
 * Protects blog load transitions, retry serialization, and route cancellation.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BlogViewModelTest {
    private val store = ViewModelStore()
    private val load = mockk<FetchOrGetBlogUseCase>()
    private val online = BlogContent(listOf(releasePost), isCached = false)

    @After
    fun restoreMainDispatcher() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `given blog view model with initial request, screen loads, expected loading then online articles`() =
        runTest {
            // Given
            val pending = CompletableDeferred<BlogContent>()
            coEvery { load() } coAnswers { Result.success(pending.await()) }
            val model = createViewModel()
            assertEquals(BlogState.Loading, model.state.value)

            // When
            pending.complete(online)
            advanceUntilIdle()

            // Then
            assertEquals(BlogState.Ready(online), model.state.value)
            coVerify(exactly = 1) { load() }
        }

    @Test
    fun `given blog view model with saved articles, cached result is loaded, expected cached flag is retained`() =
        runTest {
            // Given
            val cached = online.copy(isCached = true)
            coEvery { load() } returns Result.success(cached)

            // When
            val model = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(BlogState.Ready(cached), model.state.value)
        }

    @Test
    fun `given blog view model with failed load, retry succeeds, expected error loading and ready transitions`() =
        runTest {
            // Given
            coEvery { load() } returns Result.failure(IllegalStateException("Offline"))
            val model = createViewModel()
            advanceUntilIdle()
            assertEquals(BlogState.Error, model.state.value)
            coEvery { load() } returns Result.success(online)

            // When
            model.processIntent(BlogIntent.Retry)
            val retryState = model.state.value
            advanceUntilIdle()

            // Then
            assertEquals(BlogState.Loading, retryState)
            assertEquals(BlogState.Ready(online), model.state.value)
            coVerify(exactly = 2) { load() }
        }

    @Test
    fun `given blog view model with active retry, another retry is requested, expected just one in-flight fetch`() =
        runTest {
            // Given
            coEvery { load() } returns Result.success(online.copy(isCached = true))
            val model = createViewModel()
            advanceUntilIdle()
            val pending = CompletableDeferred<BlogContent>()
            coEvery { load() } coAnswers { Result.success(pending.await()) }

            // When
            model.processIntent(BlogIntent.Retry)
            runCurrent()
            repeat(3) { model.processIntent(BlogIntent.Retry) }
            pending.complete(online)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 2) { load() }
            assertEquals(BlogState.Ready(online), model.state.value)
        }

    @Test
    fun `given blog view model with active request, route is disposed, expected request cancels before publishing error or ready`() =
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
            assertEquals(BlogState.Loading, model.state.value)
        }

    private fun TestScope.createViewModel(): BlogViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val dispatchers = mockk<DispatchersProvider> { every { immediate } returns dispatcher }
        return BlogViewModel(mockk<UiDiagnostics>(relaxed = true), dispatchers, load).also {
            store.put("blog", it)
        }
    }
}
