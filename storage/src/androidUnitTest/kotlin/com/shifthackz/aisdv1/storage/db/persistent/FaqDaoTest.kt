package com.shifthackz.aisdv1.storage.db.persistent

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import com.shifthackz.aisdv1.storage.db.persistent.entity.FaqQuestionEntity
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
 * Verifies the FAQ snapshot against generated Room queries and transactions.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], manifest = Config.NONE)
class FaqDaoTest {
    private lateinit var database: PersistentDatabase
    private val context get() = RuntimeEnvironment.getApplication()
    private val dao get() = database.faqDao()

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
    fun `given FAQ DAO with no saved snapshot, rows are read, expected empty list`() = runTest {
        // Given
        val faqDao = dao

        // When
        val rows = faqDao.read()

        // Then
        assertTrue(rows.isEmpty())
    }

    @Test
    fun `given FAQ DAO and shuffled entries, snapshot is written and read, expected publisher ordering and exact fields`() =
        runTest {
            // Given
            val first = row(
                categoryId = "setup",
                questionId = "same",
                categoryPosition = 0,
                questionPosition = 0
            )
            val second = row(
                categoryId = "setup",
                questionId = "other",
                categoryPosition = 0,
                questionPosition = 1
            )
            val third = row(
                categoryId = "basics",
                questionId = "same",
                categoryPosition = 1,
                questionPosition = 0
            )

            // When
            dao.replace(listOf(third, second, first))
            val result = dao.read()

            // Then
            assertEquals(listOf(first, second, third), result)
        }

    @Test
    fun `given FAQ DAO with saved entries, newer snapshot replaces them, expected removed questions are absent`() =
        runTest {
            // Given
            val original = row(questionId = "old")
            val retained = row(questionId = "retained", questionPosition = 1)
            dao.replace(listOf(original, retained))
            val updated = retained.copy(question = "Updated question", questionPosition = 0)

            // When
            dao.replace(listOf(updated))

            // Then
            assertEquals(listOf(updated), dao.read())
        }

    @Test
    fun `given FAQ DAO with saved entries, replacement violates composite identity, expected previous snapshot rolls back intact`() =
        runTest {
            // Given
            val original = row(questionId = "old")
            dao.replace(listOf(original))
            val newRow = row(questionId = "new")

            // When
            val failure = runCatching {
                dao.replace(
                    listOf(
                        newRow,
                        newRow.copy(question = "Duplicate")
                    )
                )
            }.exceptionOrNull()

            // Then
            assertTrue(failure is SQLiteConstraintException)
            assertEquals(listOf(original), dao.read())
        }

    @Test
    fun `given FAQ DAO in a persistent Room database, database closes and reopens, expected saved snapshot survives`() =
        runTest {
            // Given
            val name = "faq-durable-snapshot-test"
            val stored = row()
            context.deleteDatabase(name)
            try {
                withPersistentDatabase(name) { it.faqDao().replace(listOf(stored)) }

                // When
                val restored = withPersistentDatabase(name) { it.faqDao().read() }

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

    private fun row(
        categoryId: String = "setup",
        questionId: String = "connect",
        categoryPosition: Int = 0,
        questionPosition: Int = 0,
    ) = FaqQuestionEntity(
        categoryId = categoryId,
        questionId = questionId,
        categoryLabel = categoryId.replaceFirstChar(Char::uppercaseChar),
        categoryPosition = categoryPosition,
        questionPosition = questionPosition,
        question = "Where do I connect?",
        answer = listOf("Use the provider’s URL.", "Open Configuration."),
        tags = listOf("LAN", "β"),
    )
}
