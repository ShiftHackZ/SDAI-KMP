package com.shifthackz.aisdv1.storage.db.persistent

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import com.shifthackz.aisdv1.storage.db.persistent.entity.BlogPostEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Verifies the blog snapshot against generated Room queries and transactions.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], manifest = Config.NONE)
class BlogDaoTest {
    private lateinit var database: PersistentDatabase
    private val context get() = RuntimeEnvironment.getApplication()
    private val dao get() = database.blogDao()

    @Before
    fun openDatabase() {
        database = Room.inMemoryDatabaseBuilder<PersistentDatabase>(context)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun `given blog DAO with no saved snapshot, rows are read, expected empty list`() = runTest {
        // Given
        val blogDao = dao

        // When
        val rows = blogDao.read()

        // Then
        assertTrue(rows.isEmpty())
    }

    @Test
    fun `given blog DAO and shuffled entries, snapshot is written and read, expected publisher ordering and exact fields`() =
        runTest {
            // Given
            val first = row(slug = "1.3.0-release", position = 0)
            val second = row(slug = "1.2.0-rebranding", position = 1)
            val third = row(slug = "nightly-builds", position = 2)

            // When
            dao.replace(listOf(third, second, first))
            val result = dao.read()

            // Then
            assertEquals(listOf(first, second, third), result)
        }

    @Test
    fun `given blog DAO with saved entries, newer snapshot replaces them, expected removed articles are absent`() =
        runTest {
            // Given
            val original = row(slug = "old")
            val retained = row(slug = "retained", position = 1)
            dao.replace(listOf(original, retained))
            val updated = retained.copy(title = "Updated question", position = 0)

            // When
            dao.replace(listOf(updated))

            // Then
            assertEquals(listOf(updated), dao.read())
        }

    @Test
    fun `given blog DAO with saved entries, replacement violates unique slug, expected previous snapshot rolls back intact`() =
        runTest {
            // Given
            val original = row(slug = "old")
            dao.replace(listOf(original))
            val newRow = row(slug = "new")

            // When
            val failure = runCatching {
                dao.replace(
                    listOf(
                        newRow,
                        newRow.copy(title = "Duplicate")
                    )
                )
            }.exceptionOrNull()

            // Then
            assertTrue(failure is SQLiteConstraintException)
            assertEquals(listOf(original), dao.read())
        }

    @Test
    fun `given blog DAO in a persistent Room database, database closes and reopens, expected saved snapshot survives`() =
        runTest {
            // Given
            val name = "blog-durable-snapshot-test"
            val stored = row()
            context.deleteDatabase(name)
            try {
                withPersistentDatabase(name) { it.blogDao().replace(listOf(stored)) }

                // When
                val restored = withPersistentDatabase(name) { it.blogDao().read() }

                // Then
                assertEquals(listOf(stored), restored)
            } finally {
                context.deleteDatabase(name)
            }
        }

    private suspend fun <T> withPersistentDatabase(
        name: String,
        action: suspend (PersistentDatabase) -> T
    ): T {
        val persistent = Room.databaseBuilder<PersistentDatabase>(context, name)
            .allowMainThreadQueries()
            .build()
        return try {
            action(persistent)
        } finally {
            persistent.close()
        }
    }

    private fun row(slug: String = "1.3.0-release", position: Int = 0) = BlogPostEntity(
        slug = slug,
        position = position,
        title = "SDAI release news",
        summary = "Generate offline with MNN.",
        publishedOn = "2026-11-24",
        category = "Release",
        url = "https://sdai.moroz.cc/blog/$slug/",
        coverUrl = "https://sdai.moroz.cc/blog/$slug/header.jpg",
        coverDescription = "Release artwork",
        markdown = "## Generate offline\n\nChoose **MNN**.",
    )
}
