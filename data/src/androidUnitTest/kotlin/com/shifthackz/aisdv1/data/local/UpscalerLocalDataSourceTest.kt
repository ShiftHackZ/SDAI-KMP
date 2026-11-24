package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.storage.db.persistent.dao.LocalModelDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.LocalModelEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects upscaler file completeness and independent selection while mapping persisted catalog metadata.
 *
 * @author Dmitriy Moroz
 */
class UpscalerLocalDataSourceTest {
    private val dao = mockk<LocalModelDao>()
    private val preferences = mockk<PreferenceManager>()
    private val files = mockk<DownloadableModelFileStore>()
    private val source =
        DownloadableModelLocalDataSource(dao, preferences, mockk<BuildInfoProvider>(), files)
    private val entity = LocalModelEntity(
        "upscaler_mnn_anime",
        "upscaler",
        "MNN Anime",
        "17 MB",
        listOf("https://models.example/model.mnn")
    )
    private val model = LocalAiModel(
        entity.id,
        LocalAiModel.Type.Upscaler,
        entity.name,
        entity.size,
        entity.sources,
        downloaded = true
    )

    @Test
    fun `given local upscaler catalog, downloaded selected entry is read, expected authoritative completeness and independent selection`() =
        runTest {
            // Given
            coEvery { dao.queryByType("upscaler") } returns listOf(entity)
            every { files.isDownloaded(any()) } returns true
            every { preferences.upscalerModelId } returns entity.id
            // When
            val result = source.getAllUpscalers().single()
            // Then
            assertEquals(entity.id, result.id)
            assertEquals(entity.sources, result.sources)
            assertEquals(LocalAiModel.Type.Upscaler, result.type)
            assertTrue(result.downloaded)
            assertTrue(result.selected)
            verify(exactly = 0) { preferences.localMnnModelId }
            coVerify(exactly = 1) { dao.queryByType("upscaler") }
        }

    @Test
    fun `given local upscaler catalog, payload is incomplete and another model is selected, expected neither downloaded nor selected`() =
        runTest {
            // Given
            coEvery { dao.queryByType("upscaler") } returns listOf(entity)
            every { files.isDownloaded(any()) } returns false
            every { preferences.upscalerModelId } returns "another"
            // When
            val result = source.getAllUpscalers().single()
            // Then
            assertFalse(result.downloaded)
            assertFalse(result.selected)
        }

    @Test
    fun `given local upscaler catalog, DAO read fails, expected original failure without default models`() =
        runTest {
            // Given
            val failure = IllegalStateException("DAO read failed")
            coEvery { dao.queryByType("upscaler") } throws failure
            // When
            val result = runCatching { source.getAllUpscalers() }
            // Then
            assertSame(failure, result.exceptionOrNull())
        }

    @Test
    fun `given upscaler checkpoint lookup, completed catalog entry has sources, expected owned file store path`() {
        // Given
        every { files.resolvePath(model) } returns "/owned/model.mnn"
        // When
        val result = source.getModelPath(model)
        // Then
        assertEquals("/owned/model.mnn", result)
        verify(exactly = 1) { files.resolvePath(model) }
    }

    @Test
    fun `given upscaler checkpoint lookup, model is incomplete or has no source, expected rejection without resolving files`() {
        // Given
        val invalid = listOf(model.copy(downloaded = false), model.copy(sources = emptyList()))
        // When
        val failures = invalid.map { runCatching { source.getModelPath(it) }.exceptionOrNull() }
        // Then
        assertTrue(failures.all { it is IllegalArgumentException })
        verify(exactly = 0) { files.resolvePath(any()) }
    }

    @Test
    fun `given upscaler catalog persistence, dedicated metadata is saved, expected model type and source list preserved`() =
        runTest {
            // Given
            coEvery { dao.insertList(any()) } returns Unit
            // When
            source.save(listOf(model))
            // Then
            coVerify(exactly = 1) { dao.insertList(listOf(entity)) }
        }
}
