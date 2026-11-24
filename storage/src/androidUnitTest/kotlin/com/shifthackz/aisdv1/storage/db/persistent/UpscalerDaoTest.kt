package com.shifthackz.aisdv1.storage.db.persistent

import androidx.room.Room
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.LocalModelEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Exercises generated Room queries for dedicated model catalogs and upscaler Gallery provenance.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], manifest = Config.NONE)
class UpscalerDaoTest {
    private lateinit var database: PersistentDatabase
    private val model
        get() = LocalModelEntity(
            "upscaler_mnn_anime",
            "upscaler",
            "MNN Anime",
            "17 MB",
            listOf("https://models.example/primary.mnn", "https://models.example/mirror.mnn")
        )
    private val result
        get() = GenerationResultEntity(
            id = 0, imageBase64 = "upscaled-pixels", originalImageBase64 = "original-pixels",
            createdAt = 100, generationType = "upscale", prompt = "", negativePrompt = "",
            width = 4096, height = 3072, samplingSteps = 0, cfgScale = 0f, restoreFaces = false,
            sampler = "", seed = "", subSeed = "", subSeedStrength = 0f, denoisingStrength = 0f,
            hidden = false, providerKey = "local_mnn_upscaler", imageRevision = "revision",
            upscalerModelName = "Real-ESRGAN Anime 6B (MNN)",
        )

    @Before
    fun openDatabase() {
        database =
            Room.inMemoryDatabaseBuilder<PersistentDatabase>(RuntimeEnvironment.getApplication())
                .allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun `given model DAO, upscaler and diffusion catalogs are saved, expected dedicated family isolation and ordered mirror sources`() =
        runTest {
            // Given
            val dao = database.localModelDao()
            dao.insertList(listOf(model, model.copy(id = "diffusion", type = "mnn")))
            // When
            val upscalers = dao.queryByType("upscaler")
            // Then
            assertEquals(listOf(model), upscalers)
            assertEquals(listOf("diffusion"), dao.queryByType("mnn").map { it.id })
        }

    @Test
    fun `given model DAO, an upscaler catalog entry is refreshed, expected replacement metadata without duplicate or diffusion mutation`() =
        runTest {
            // Given
            val dao = database.localModelDao()
            val generation = model.copy(id = "diffusion", type = "mnn")
            dao.insertList(listOf(model, generation))
            val refreshed = model.copy(
                name = "Updated Anime",
                sources = listOf("https://models.example/new.mnn")
            )
            // When
            dao.insert(refreshed)
            // Then
            assertEquals(listOf(refreshed), dao.queryByType("upscaler"))
            assertEquals(generation, dao.queryById("diffusion"))
            assertEquals(2, dao.query().size)
        }

    @Test
    fun `given model DAO observer, upscaler metadata is committed, expected typed flow emits persisted catalog`() =
        runTest {
            // Given
            val dao = database.localModelDao()
            assertTrue(dao.observeByType("upscaler").first().isEmpty())
            // When
            dao.insert(model)
            // Then
            assertEquals(listOf(model), dao.observeByType("upscaler").first())
        }

    @Test
    fun `given Gallery DAO, standalone upscaled result is saved, expected original pixels dimensions and provider provenance round trip`() =
        runTest {
            // Given
            val dao = database.generationResultDao()
            // When
            val id = dao.insert(result)
            val restored = dao.query().single()
            // Then
            assertEquals(result.copy(id = id), restored)
            assertEquals("original-pixels", restored.originalImageBase64)
        }

    @Test
    fun `given Gallery DAO, upscaled result metadata is projected, expected upscaler checkpoint preserved in window item and history queries`() =
        runTest {
            // Given
            val dao = database.generationResultDao()
            val id = dao.insert(result)
            // When
            val item = dao.observeMetadataItem(id).first()
            val window = dao.observeMetadata(20, 0).first().single()
            val history = dao.queryHistoryMetadata().single()
            val selected = dao.queryMetadata(listOf(id)).single()
            // Then
            assertEquals(result.upscalerModelName, item?.upscalerModelName)
            assertEquals(result.upscalerModelName, window.upscalerModelName)
            assertEquals(result.upscalerModelName, history.upscalerModelName)
            assertEquals(result.upscalerModelName, selected.upscalerModelName)
            assertEquals(4096, selected.width)
            assertEquals(3072, selected.height)
        }

    @Test
    fun `given Gallery DAO, regular generation and hidden upscaled result are saved, expected nullable provenance and hidden history exclusion`() =
        runTest {
            // Given
            val dao = database.generationResultDao()
            dao.insert(result.copy(generationType = "text_to_image", upscalerModelName = null))
            dao.insert(result.copy(hidden = true))
            // When
            val history = dao.queryHistoryMetadata()
            // Then
            assertEquals(1, history.size)
            assertNull(history.single().upscalerModelName)
        }
}
