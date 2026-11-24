package com.shifthackz.aisdv1.storage.db.persistent

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Opens the actual previous schema through Room's generated upscaler provenance migration.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], manifest = Config.NONE)
class UpscalerMigrationTest {
    @Test
    fun `given version 21 Gallery data, Room migrates to version 22, expected preserved originals and nullable upscaler provenance`() =
        runTest {
            // Given
            val context = RuntimeEnvironment.getApplication()
            val name = "upscaler-migration"
            context.deleteDatabase(name)
            val file = context.getDatabasePath(name)
            file.parentFile?.mkdirs()
            try {
                SQLiteDatabase.openOrCreateDatabase(file, null).use { previous ->
                    val resource =
                        "/com.shifthackz.aisdv1.storage.db.persistent.PersistentDatabase/21.json"
                    val schema = JSONObject(
                        requireNotNull(javaClass.getResourceAsStream(resource)).bufferedReader()
                            .use { it.readText() })
                        .getJSONObject("database")
                    val entities = schema.getJSONArray("entities")
                    for (index in 0 until entities.length()) {
                        val entity = entities.getJSONObject(index)
                        val table = entity.getString("tableName")
                        previous.execSQL(
                            entity.getString("createSql").replace("\${TABLE_NAME}", table)
                        )
                        val indices = entity.optJSONArray("indices") ?: continue
                        for (position in 0 until indices.length()) {
                            previous.execSQL(
                                indices.getJSONObject(position).getString("createSql")
                                    .replace("\${TABLE_NAME}", table)
                            )
                        }
                    }
                    previous.execSQL(
                        """
                    INSERT INTO generation_results(
                    id, image_base_64, original_image_base_64, created_at, generation_type,
                    prompt, negative_prompt, width, height, sampling_steps, cfg_scale,
                    restore_faces, sampler, seed
                    ) VALUES (1, 'saved-pixels', 'original-pixels', 100, 'text_to_image',
                    'saved prompt', '', 512, 512, 20, 7.0, 0, 'Euler', '42')
                """.trimIndent()
                    )
                    previous.version = 21
                }
                // When
                val migrated =
                    Room.databaseBuilder<PersistentDatabase>(context, name).allowMainThreadQueries()
                        .build()
                try {
                    val row = migrated.generationResultDao().query().single()
                    val metadata = migrated.generationResultDao().observeMetadataItem(1).first()
                    // Then
                    assertEquals("saved-pixels", row.imageBase64)
                    assertEquals("original-pixels", row.originalImageBase64)
                    assertEquals("saved prompt", row.prompt)
                    assertNull(row.upscalerModelName)
                    assertNull(metadata?.upscalerModelName)
                } finally {
                    migrated.close()
                }
            } finally {
                context.deleteDatabase(name)
            }
        }
}
