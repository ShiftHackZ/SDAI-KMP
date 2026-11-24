package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.data.releasePost
import com.shifthackz.aisdv1.storage.db.persistent.dao.BlogDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.BlogPostEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks the full article payload and publisher order across the Room mapping boundary.
 *
 * @author Dmitriy Moroz
 */
class BlogLocalDataSourceTest {
    private val dao = mockk<BlogDao>()
    private val source = BlogLocalDataSource(dao)
    private val releaseRow = BlogPostEntity(
        slug = "1.3.0-release",
        position = 0,
        title = "SDAI 1.3.0",
        summary = "Generate offline.",
        publishedOn = "2026-11-24",
        category = "Release",
        url = "https://sdai.moroz.cc/blog/1.3.0-release/",
        coverUrl = "https://sdai.moroz.cc/blog/1.3.0-release/header.jpg",
        coverDescription = "Release artwork",
        markdown = "## Generate on your phone\n\nChoose **MNN**.",
    )

    @Test
    fun `given blog local data source with saved articles, snapshot is read, expected complete domain posts in stored order`() =
        runTest {
            // Given
            coEvery { dao.read() } returns listOf(
                releaseRow,
                releaseRow.copy(slug = "nightly", position = 1)
            )

            // When
            val result = source.read().getOrThrow()

            // Then
            assertEquals(listOf(releasePost, releasePost.copy(slug = "nightly")), result)
        }

    @Test
    fun `given blog local data source with two articles, feed is saved, expected full rows with publisher positions`() =
        runTest {
            // Given
            val posts = listOf(releasePost, releasePost.copy(slug = "nightly", coverUrl = ""))
            coEvery { dao.replace(any()) } returns Unit

            // When
            source.save(posts).getOrThrow()

            // Then
            coVerify(exactly = 1) {
                dao.replace(
                    listOf(
                        releaseRow,
                        releaseRow.copy(slug = "nightly", position = 1, coverUrl = "")
                    )
                )
            }
        }

    @Test
    fun `given blog local data source and successful empty feed, snapshot is saved, expected empty replacement reaches Room`() =
        runTest {
            // Given
            coEvery { dao.replace(emptyList()) } returns Unit

            // When
            source.save(emptyList()).getOrThrow()

            // Then
            coVerify(exactly = 1) { dao.replace(emptyList()) }
        }
}
