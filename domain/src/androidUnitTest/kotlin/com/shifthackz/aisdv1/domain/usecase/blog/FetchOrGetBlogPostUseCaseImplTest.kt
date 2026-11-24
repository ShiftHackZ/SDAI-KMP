package com.shifthackz.aisdv1.domain.usecase.blog

import com.shifthackz.aisdv1.domain.entity.BlogContent
import com.shifthackz.aisdv1.domain.repository.BlogRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Covers article lookup from the saved snapshot and explicit feed refresh.
 *
 * @author Dmitriy Moroz
 */
class FetchOrGetBlogPostUseCaseImplTest {
    private val repository = mockk<BlogRepository>()
    private val feed = mockk<FetchOrGetBlogUseCase>()
    private val useCase = FetchOrGetBlogPostUseCaseImpl(repository, feed)
    private val slug = releaseArticle.slug

    @Test
    fun `given blog post use case and saved article, reader opens, expected snapshot article without requesting the feed`() =
        runTest {
            // Given
            coEvery { repository.readCache() } returns Result.success(listOf(releaseArticle))

            // When
            val article = useCase(slug).getOrThrow()

            // Then
            assertEquals(releaseArticle, article)
            coVerify(exactly = 0) { feed() }
        }

    @Test
    fun `given blog post use case and cache read failure, reader opens, expected article resolved from the shared feed`() =
        runTest {
            // Given
            coEvery { repository.readCache() } returns Result.failure(IllegalStateException("Cache unavailable"))
            coEvery { feed() } returns Result.success(
                BlogContent(
                    listOf(releaseArticle),
                    isCached = false
                )
            )

            // When
            val article = useCase(slug).getOrThrow()

            // Then
            assertEquals(releaseArticle, article)
            coVerifySequence { repository.readCache(); feed() }
        }

    @Test
    fun `given blog post use case and a revised article, refresh is requested, expected latest revision without reading saved snapshot first`() =
        runTest {
            // Given
            val updated = releaseArticle.copy(markdown = "Updated release notes.")
            coEvery { feed() } returns Result.success(
                BlogContent(
                    listOf(updated),
                    isCached = false
                )
            )

            // When
            val article = useCase(slug, shouldRefresh = true).getOrThrow()

            // Then
            assertEquals(updated, article)
            coVerify(exactly = 0) { repository.readCache() }
        }

    @Test
    fun `given blog post use case and retired slug, cached and current lists are checked, expected no article`() =
        runTest {
            // Given
            coEvery { repository.readCache() } returns Result.success(emptyList())
            coEvery { feed() } returns Result.success(
                BlogContent(
                    listOf(releaseArticle),
                    isCached = false
                )
            )

            // When
            val article = useCase("retired-post").getOrThrow()

            // Then
            assertNull(article)
            coVerifySequence { repository.readCache(); feed() }
        }

    @Test
    fun `given blog post use case during a cancelled snapshot read, reader opens, expected cancellation without fetching`() =
        runTest {
            // Given
            val cancellation = CancellationException("Reader closed")
            coEvery { repository.readCache() } throws cancellation

            // When
            val result = runCatching { useCase(slug) }

            // Then
            assertSame(cancellation, result.exceptionOrNull())
            coVerify(exactly = 0) { feed() }
        }
}
