package com.shifthackz.aisdv1.storage.db.persistent

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Validates the generated blog migration against the exported version 20 schema.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], manifest = Config.NONE)
class BlogMigrationTest {

    @Test
    fun `given persistent database version 20 with FAQ data, Room opens version 21, expected empty blog cache and preserved FAQ`() =
        runTest {
            // Given
            val context = RuntimeEnvironment.getApplication()
            val name = "blog-migration-test"
            val file = context.getDatabasePath(name)
            context.deleteDatabase(name)
            file.parentFile?.mkdirs()
            try {
                SQLiteDatabase.openOrCreateDatabase(file, null).use(::createPreviousSchema)

                // When
                val migrated = Room.databaseBuilder<PersistentDatabase>(context, name)
                    .allowMainThreadQueries()
                    .build()
                try {
                    val posts = migrated.blogDao().read()
                    val questions = migrated.faqDao().read()

                    // Then
                    assertTrue(posts.isEmpty())
                    assertEquals("connect", questions.single().questionId)
                    assertEquals(listOf("Open Settings."), questions.single().answer)
                } finally {
                    migrated.close()
                }
            } finally {
                context.deleteDatabase(name)
            }
        }

    private fun createPreviousSchema(database: SQLiteDatabase) {
        val schemaPath = "/com.shifthackz.aisdv1.storage.db.persistent.PersistentDatabase/20.json"
        val source = requireNotNull(javaClass.getResourceAsStream(schemaPath))
            .bufferedReader().use { it.readText() }
        val schema = JSONObject(source).getJSONObject("database")
        val entities = schema.getJSONArray("entities")
        for (index in 0 until entities.length()) {
            val entity = entities.getJSONObject(index)
            val tableName = entity.getString("tableName")
            database.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", tableName))
            val indices = entity.optJSONArray("indices") ?: continue
            for (indexPosition in 0 until indices.length()) {
                database.execSQL(
                    indices.getJSONObject(indexPosition).getString("createSql")
                        .replace("\${TABLE_NAME}", tableName),
                )
            }
        }
        val setupQueries = schema.getJSONArray("setupQueries")
        for (index in 0 until setupQueries.length()) {
            database.execSQL(setupQueries.getString(index))
        }
        database.execSQL(
            """
            INSERT INTO faq_questions (
                categoryId, questionId, categoryLabel, categoryPosition, questionPosition, question, answer, tags
            ) VALUES ('setup', 'connect', 'Setup', 0, 0, 'How do I connect?', '["Open Settings."]', '[]')
            """.trimIndent(),
        )
        database.version = schema.getInt("version")
    }
}
