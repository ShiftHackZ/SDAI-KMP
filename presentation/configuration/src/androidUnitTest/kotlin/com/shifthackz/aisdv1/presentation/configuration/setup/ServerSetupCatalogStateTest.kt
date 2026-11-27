package com.shifthackz.aisdv1.presentation.configuration.setup

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Applies pending choice and transfer race contracts independently to every local provider.
 *
 * @param type Local family owning the cached response and its in-flight operations.
 *
 * @author Dmitriy Moroz
 */
@RunWith(Parameterized::class)
class ServerSetupCatalogStateTest(private val type: LocalAiModel.Type) {
    private val model = LocalAiModel("catalog", type, "Model", "1 GB", emptyList(), selected = true)

    @Test
    fun `given catalog completion, user has pending selection, expected choice retained without mode change`() {
        // Given
        val initial = ServerSetupState(mode = ServerSource.AUTOMATIC1111).withCatalogResult(
            type,
            listOf(model),
            LocalCatalogStatus.READY
        )
        val selected = initial.copy(localCatalogSelectionOverrides = mapOf(type to model.id))

        // When
        val refreshed = selected.withCatalogResult(
            type,
            listOf(model.copy(selected = false)),
            LocalCatalogStatus.READY
        )

        // Then
        assertEquals(true, refreshed.catalogModels(type).single().selected)
        assertEquals(ServerSource.AUTOMATIC1111, refreshed.mode)
    }

    @Test
    fun `given catalog completion, download remains active, expected partial cache never connectable`() {
        // Given
        val initial =
            ServerSetupState().withCatalogResult(type, listOf(model), LocalCatalogStatus.READY)
        val active = initial.withUpdatedLocalModel(
            initial.catalogModels(type).single()
                .copy(downloadState = DownloadState.Downloading(45)), type
        )

        // When
        val refreshed = active.withCatalogResult(
            type,
            listOf(model.copy(downloaded = true)),
            LocalCatalogStatus.READY
        )

        // Then
        assertEquals(false, refreshed.catalogModels(type).single().downloaded)
        assertEquals(
            DownloadState.Downloading(45),
            refreshed.catalogModels(type).single().downloadState
        )
    }

    @Test
    fun `given catalog completion, model was explicitly deleted, expected stale downloaded result suppressed`() {
        // Given
        val initial = ServerSetupState().withCatalogResult(
            type,
            listOf(model.copy(downloaded = true)),
            LocalCatalogStatus.READY
        )
        val deleted = initial.withDeletedLocalModel(initial.catalogModels(type).single(), type)

        // When
        val refreshed = deleted.withCatalogResult(
            type,
            listOf(model.copy(downloaded = true)),
            LocalCatalogStatus.READY
        )

        // Then
        assertEquals(false, refreshed.catalogModels(type).single().downloaded)
        assertEquals(false, refreshed.catalogModels(type).single().selected)
        assertEquals(DownloadState.Unknown, refreshed.catalogModels(type).single().downloadState)
    }

    @Test
    fun `given catalog refresh, remote catalog fails, expected existing usable rows retained with error`() {
        // Given
        val initial = ServerSetupState().withCatalogResult(
            type,
            listOf(model.copy(downloaded = true)),
            LocalCatalogStatus.READY
        )

        // When
        val failed = initial.withCatalogResult(type, emptyList(), LocalCatalogStatus.ERROR)

        // Then
        assertEquals(initial.catalogModels(type), failed.catalogModels(type))
        assertEquals(LocalCatalogStatus.ERROR, failed.localCatalogStatuses[type])
    }

    @Test
    fun `given cancelled download recovery, verified install exists after earlier delete, expected tombstones and active markers cleared`() {
        // Given
        val initial =
            ServerSetupState().withCatalogResult(type, listOf(model), LocalCatalogStatus.READY)
        val deleted = initial.withDeletedLocalModel(initial.catalogModels(type).single(), type)
        val active = deleted.withUpdatedLocalModel(
            deleted.catalogModels(type).single()
                .copy(downloadState = DownloadState.Downloading(99)), type
        )

        // When
        val recovered = active.withVerifiedOwnedModel(model.id, type).withCatalogResult(
            type,
            listOf(model.copy(downloaded = true)),
            LocalCatalogStatus.READY
        )

        // Then
        assertEquals(true, recovered.catalogModels(type).single().downloaded)
        assertEquals(DownloadState.Unknown, recovered.catalogModels(type).single().downloadState)
        assertEquals(emptySet<String>(), recovered.localCatalogDeletedModelIds[type])
        assertEquals(emptySet<String>(), recovered.localCatalogActiveDownloads[type])
    }

    companion object {
        /**
         * Keeps the shared state contract exercised for every runtime family.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun catalogs(): List<Array<LocalAiModel.Type>> =
            LocalAiModel.Type.entries.filter { it != LocalAiModel.Type.Upscaler }
                .map { arrayOf(it) }
    }
}
