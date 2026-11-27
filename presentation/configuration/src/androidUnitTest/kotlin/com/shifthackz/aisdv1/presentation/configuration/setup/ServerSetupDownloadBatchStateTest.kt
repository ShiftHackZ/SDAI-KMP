package com.shifthackz.aisdv1.presentation.configuration.setup

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ServerSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Verifies independent row ownership and conservative terminal replay for every local catalog family.
 *
 * @param type Owning provider to update even when setup displays a remote provider.
 * @author Dmitriy Moroz
 */
@RunWith(Parameterized::class)
class ServerSetupDownloadBatchStateTest(private val type: LocalAiModel.Type) {
    private val first = LocalAiModel(
        "first",
        type,
        "First",
        "1 GB",
        listOf("https://models/first"),
        selected = true
    )
    private val second = first.copy(id = "second", selected = false)
    private val initial
        get() = ServerSetupState(mode = ServerSource.AUTOMATIC1111)
            .withCatalogResult(type, listOf(first, second), LocalCatalogStatus.READY)
    private val firstWork
        get() = ModelDownloadBatch(
            type,
            first.id,
            DownloadState.Downloading(30),
            isBatch = false,
            workId = "first-work"
        )
    private val secondWork
        get() = ModelDownloadBatch(
            type,
            second.id,
            DownloadState.Downloading(60),
            isBatch = false,
            workId = "second-work"
        )

    @Test
    fun `given concurrent model progress, both snapshots arrive, expected separate rows and setup selection preserved`() {
        // Given
        val state = initial
        // When
        val result = state.withModelDownloadBatches(listOf(firstWork, secondWork))
        // Then
        assertEquals(
            listOf(DownloadState.Downloading(30), DownloadState.Downloading(60)),
            result.catalogModels(type).map { it.downloadState })
        assertEquals(setOf(first.id, second.id), result.localCatalogActiveDownloads[type])
        assertTrue(result.catalogModels(type).first().selected)
        assertEquals(ServerSource.AUTOMATIC1111, result.mode)
    }

    @Test
    fun `given concurrent model progress, one worker is cancelled, expected other row continues without catalog loading`() {
        // Given
        val state = initial.withModelDownloadBatches(listOf(firstWork, secondWork))
        // When
        val result = state.withModelDownloadBatches(
            listOf(
                firstWork.copy(
                    progress = DownloadState.Unknown,
                    isRunning = false
                ), secondWork.copy(progress = DownloadState.Downloading(70))
            )
        )
        // Then
        assertEquals(
            listOf(DownloadState.Unknown, DownloadState.Downloading(70)),
            result.catalogModels(type).map { it.downloadState })
        assertEquals(setOf(second.id), result.localCatalogActiveDownloads[type])
        assertEquals(LocalCatalogStatus.READY, result.localCatalogStatuses[type])
    }

    @Test
    fun `given provider batch, next model starts after first activation, expected first owned and second transferring`() {
        // Given
        val batch = ModelDownloadBatch(
            type,
            second.id,
            DownloadState.Downloading(5),
            listOf(first.id),
            totalModelCount = 2
        )
        // When
        val result = initial.withModelDownloadBatches(listOf(batch))
        // Then
        assertTrue(result.catalogModels(type).first().downloaded)
        assertFalse(result.catalogModels(type).last().downloaded)
        assertEquals(DownloadState.Downloading(5), result.catalogModels(type).last().downloadState)
    }

    @Test
    fun `given deleted local model, historical worker success replays, expected current ownership remains deleted`() {
        // Given
        val state = initial.withDeletedLocalModel(initial.catalogModels(type).first(), type)
        val history = firstWork.copy(
            progress = DownloadState.Complete("/owned/old"),
            completedIds = listOf(first.id),
            isRunning = false
        )
        // When
        val result = state.withModelDownloadBatches(listOf(history))
        // Then
        assertFalse(result.catalogModels(type).first().downloaded)
        assertEquals(DownloadState.Unknown, result.catalogModels(type).first().downloadState)
    }

    @Test
    fun `given active worker, metadata refresh reports partial directory as owned, expected worker progress overrides cache`() {
        // Given
        val state = initial.withModelDownloadBatches(listOf(firstWork))
        // When
        val result = state.withCatalogResult(
            type,
            listOf(first.copy(downloaded = true), second),
            LocalCatalogStatus.ERROR
        )
        // Then
        assertFalse(result.catalogModels(type).first().downloaded)
        assertEquals(
            DownloadState.Downloading(30),
            result.catalogModels(type).first().downloadState
        )
        assertEquals(LocalCatalogStatus.ERROR, result.localCatalogStatuses[type])
    }

    companion object {
        /**
         * Supplies all common catalog families, including those unavailable on the current host platform.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun providers() = LocalAiModel.Type.entries.filter { it != LocalAiModel.Type.Upscaler }
            .map { arrayOf(it) }
    }
}
