package com.shifthackz.aisdv1.presentation.blog.detail

import androidx.lifecycle.ViewModelStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.domain.usecase.blog.FetchOrGetBlogPostUseCase
import com.shifthackz.aisdv1.presentation.blog.detail.share.BlogPlatformActions
import com.shifthackz.aisdv1.presentation.blog.releasePost
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Exercises reader load, refresh, missing article and sharing behavior.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BlogDetailViewModelTest {
    private val load = mockk<FetchOrGetBlogPostUseCase>()
    private val actions = mockk<BlogPlatformActions>()
    private val store = ViewModelStore()
    private val slug = releasePost.slug

    @After
    fun restoreMainDispatcher() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `given blog detail view model with no matching article, reader opens, expected missing state and no share action`() =
        runTest {
            // Given
            coEvery { load(slug, false) } returns Result.success(null)

            // When
            val model = createViewModel()
            advanceUntilIdle()
            model.processIntent(BlogDetailIntent.Share)

            // Then
            assertEquals(BlogDetailState.Missing, model.state.value)
            verify(exactly = 0) { actions.sharePost(any()) }
        }

    @Test
    fun `given blog detail view model after load fails, Retry is requested, expected refresh returns the current article`() =
        runTest {
            // Given
            coEvery { load(slug, false) } returns Result.failure(IllegalStateException("Offline"))
            coEvery { load(slug, true) } returns Result.success(releasePost)
            val model = createViewModel()
            advanceUntilIdle()
            assertEquals(BlogDetailState.Error, model.state.value)

            // When
            model.processIntent(BlogDetailIntent.Retry)
            val retryState = model.state.value
            advanceUntilIdle()

            // Then
            assertEquals(BlogDetailState.Loading, retryState)
            assertEquals(BlogDetailState.Ready(releasePost), model.state.value)
            coVerifySequence {
                load(slug, false)
                load(slug, true)
            }
        }

    @Test
    fun `given blog detail view model with a displayed article, share is requested, expected title and canonical URL on separate lines`() =
        runTest {
            // Given
            coEvery { load(slug, false) } returns Result.success(releasePost)
            every { actions.sharePost(any()) } returns true
            val model = createViewModel()
            advanceUntilIdle()

            // When
            model.processIntent(BlogDetailIntent.Share)

            // Then
            verify(exactly = 1) {
                actions.sharePost("SDAI 1.3.0\nhttps://sdai.moroz.cc/blog/1.3.0-release/")
            }
        }

    @Test
    fun `given blog detail view model with a displayed article, share sheet cannot open, expected share failure effect`() =
        runTest {
            // Given
            coEvery { load(slug, false) } returns Result.success(releasePost)
            every { actions.sharePost(any()) } returns false
            val model = createViewModel()
            advanceUntilIdle()

            // When
            model.processIntent(BlogDetailIntent.Share)
            val effect = withTimeout(1_000) { model.effect.first() }

            // Then
            assertEquals(BlogDetailEffect.ShareFailed, effect)
        }

    private fun TestScope.createViewModel(): BlogDetailViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val dispatchers = mockk<DispatchersProvider> { every { immediate } returns dispatcher }
        return BlogDetailViewModel(
            mockk<UiDiagnostics>(relaxed = true),
            dispatchers,
            load,
            actions,
            slug
        )
            .also { store.put("blog-detail", it) }
    }
}
