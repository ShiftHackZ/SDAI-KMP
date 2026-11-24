package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.data.releasePost
import com.shifthackz.aisdv1.network.api.sdai.SdaiAppApi
import com.shifthackz.aisdv1.network.api.sdai.model.BlogResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Checks the published feed's mapping to domain posts.
 *
 * @author Dmitriy Moroz
 */
class BlogRemoteDataSourceTest {
    private val api = mockk<SdaiAppApi>()
    private val source = BlogRemoteDataSource(api)

    @Test
    fun `given blog remote data source with a dotted release slug and repeated entries, feed is mapped, expected all fields and order are preserved`() =
        runTest {
            // Given
            val published = BlogResponse.Post(
                slug = "1.3.0-release",
                title = "SDAI 1.3.0",
                summary = "Generate offline.",
                publishedOn = "2026-11-24",
                category = "Release",
                url = "https://sdai.moroz.cc/blog/1.3.0-release/",
                coverUrl = "https://sdai.moroz.cc/blog/1.3.0-release/header.jpg",
                coverDescription = "Release artwork",
                markdown = "## Generate on your phone\n\nChoose **MNN**.",
            )
            coEvery { api.fetchBlog() } returns BlogResponse(
                listOf(published, published.copy(title = "Updated headline", coverUrl = "")),
            )

            // When
            val result = source.fetch().getOrThrow()

            // Then
            assertEquals(
                listOf(
                    releasePost,
                    releasePost.copy(title = "Updated headline", coverUrl = "")
                ), result
            )
        }

    @Test
    fun `given blog remote data source with no published posts, feed is fetched, expected empty list`() =
        runTest {
            // Given
            coEvery { api.fetchBlog() } returns BlogResponse(emptyList())

            // When
            val result = source.fetch().getOrThrow()

            // Then
            assertEquals(emptyList<Any>(), result)
        }

    @Test
    fun `given blog remote data source and an HTTP error, feed is fetched, expected original failure reaches repository`() =
        runTest {
            // Given
            val failure = IllegalStateException("Offline")
            coEvery { api.fetchBlog() } throws failure

            // When
            val result = source.fetch()

            // Then
            assertSame(failure, result.exceptionOrNull())
        }
}
