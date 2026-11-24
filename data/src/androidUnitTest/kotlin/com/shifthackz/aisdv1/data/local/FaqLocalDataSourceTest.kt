package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import com.shifthackz.aisdv1.storage.db.persistent.dao.FaqDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.FaqQuestionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks ordered mapping between FAQ domain categories and the persistent snapshot rows.
 *
 * @author Dmitriy Moroz
 */
class FaqLocalDataSourceTest {
    private val dao = mockk<FaqDao>()
    private val source = FaqLocalDataSource(dao)
    private val question = FaqQuestion(
        "connection",
        "How do I connect?",
        listOf("Open Configuration.", "Enter the URL."),
        listOf("server")
    )
    private val second = question.copy(id = "address", question = "What URL?")
    private val categories = listOf(
        FaqCategory("setup", "Setup", listOf(question, second)),
        FaqCategory("basics", "Basics", listOf(question)),
    )
    private val rows = listOf(
        row("setup", "Setup", 0, 0, "connection", "How do I connect?"),
        row("setup", "Setup", 0, 1, "address", "What URL?"),
        row("basics", "Basics", 1, 0, "connection", "How do I connect?"),
    )

    @Test
    fun `given FAQ local data source and ordered rows, cache is read, expected original categories and question fields`() =
        runTest {
            // Given
            coEvery { dao.read() } returns rows

            // When
            val snapshot = source.read()

            // Then
            assertEquals(categories, snapshot)
        }

    @Test
    fun `given FAQ local data source before the first save, cache is read, expected empty domain list`() =
        runTest {
            // Given
            coEvery { dao.read() } returns emptyList()

            // When
            val snapshot = source.read()

            // Then
            assertTrue(snapshot.isEmpty())
        }

    @Test
    fun `given FAQ local data source and domain categories, cache is saved, expected one replacement with ordered rows`() =
        runTest {
            // Given
            coEvery { dao.replace(any()) } returns Unit

            // When
            source.save(categories)

            // Then
            coVerify(exactly = 1) { dao.replace(rows) }
        }

    @Test
    fun `given FAQ local data source and Room failures, cache is read and written, expected original failures propagate`() =
        runTest {
            // Given
            val readFailure = IllegalStateException("Read failed")
            val writeFailure = IllegalStateException("Write failed")
            coEvery { dao.read() } throws readFailure
            coEvery { dao.replace(any()) } throws writeFailure

            // When
            val readResult = runCatching { source.read() }
            val writeResult = runCatching { source.save(categories) }

            // Then
            assertSame(readFailure, readResult.exceptionOrNull())
            assertSame(writeFailure, writeResult.exceptionOrNull())
        }

    private fun row(
        categoryId: String,
        label: String,
        categoryIndex: Int,
        questionIndex: Int,
        id: String,
        title: String,
    ) = FaqQuestionEntity(
        categoryId,
        id,
        label,
        categoryIndex,
        questionIndex,
        title,
        question.answer,
        question.tags
    )
}
