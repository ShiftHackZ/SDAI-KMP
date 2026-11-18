package com.shifthackz.aisdv1.presentation.configuration.setup.component

import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.withCatalogResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects footer visibility and enabled click semantics without asserting animation implementation details.
 * @author Dmitriy Moroz
 */
class LocalModelDownloadActionStateTest {
    private val type = LocalAiModel.Type.ONNX
    private val model =
        LocalAiModel("catalog", type, "Model", "1 GB", listOf("https://models/model"))
    private val state
        get() = ServerSetupState(
            mode = ServerSource.LOCAL_MICROSOFT_ONNX,
            loadingConfiguration = false
        )
            .withCatalogResult(type, listOf(model), LocalCatalogStatus.READY)

    @Test
    fun `given missing catalog model, no worker is active, expected enabled download all action`() {
        // Given
        val initial = state
        // When
        val action = initial.toLocalModelDownloadActionState()
        // Then
        assertEquals(LocalModelDownloadActionState(true, true, false), action)
    }

    @Test
    fun `given single model work, current provider starts transfer, expected batch action hidden until termination`() {
        // Given
        val work = ModelDownloadBatch(type, model.id, isBatch = false)
        val active = state.copy(modelDownloadBatches = listOf(work))
        // When
        val during = active.toLocalModelDownloadActionState()
        val after = active.copy(modelDownloadBatches = listOf(work.copy(isRunning = false)))
            .toLocalModelDownloadActionState()
        // Then
        assertFalse(during.isVisible)
        assertEquals(LocalModelDownloadActionState(true, true, false), after)
    }

    @Test
    fun `given active provider batch, all catalog entries finish activation, expected cancel all action retained until worker stops`() {
        // Given
        val completed = state.withCatalogResult(
            type,
            listOf(model.copy(downloaded = true)),
            LocalCatalogStatus.READY
        )
            .copy(modelDownloadBatches = listOf(ModelDownloadBatch(type)))
        // When
        val action = completed.toLocalModelDownloadActionState()
        // Then
        assertEquals(LocalModelDownloadActionState(true, true, true), action)
    }

    @Test
    fun `given another provider download, current provider has missing models, expected enabled action for switch dialog`() {
        // Given
        val initial =
            state.copy(modelDownloadBatches = listOf(ModelDownloadBatch(LocalAiModel.Type.MediaPipe)))
        // When
        val action = initial.toLocalModelDownloadActionState()
        // Then
        assertEquals(LocalModelDownloadActionState(true, true, false), action)
    }

    @Test
    fun `given completed catalog, no missing models remain, expected download all hidden`() {
        // Given
        val completed = state.withCatalogResult(
            type,
            listOf(model.copy(downloaded = true)),
            LocalCatalogStatus.READY
        )
        // When
        val action = completed.toLocalModelDownloadActionState()
        // Then
        assertFalse(action.isVisible)
    }

    @Test
    fun `given model without sources, catalog is ready, expected download all hidden`() {
        // Given
        val initial = state.withCatalogResult(
            type,
            listOf(model.copy(sources = emptyList())),
            LocalCatalogStatus.READY
        )
        // When
        val action = initial.toLocalModelDownloadActionState()
        // Then
        assertFalse(action.isVisible)
    }

    @Test
    fun `given failed catalog, no work is running, expected visible download action inactive`() {
        // Given
        val initial = state.copy(localCatalogStatuses = mapOf(type to LocalCatalogStatus.ERROR))
        // When
        val action = initial.toLocalModelDownloadActionState()
        // Then
        assertTrue(action.isVisible)
        assertFalse(action.isEnabled)
    }

    @Test
    fun `given configuration initialization, catalog has missing models, expected footer disabled`() {
        // Given
        val initial = state.copy(loadingConfiguration = true)
        // When
        val action = initial.toLocalModelDownloadActionState()
        // Then
        assertFalse(action.isEnabled)
    }

    @Test
    fun `given remote provider, model downloads exist, expected local footer absent`() {
        // Given
        val initial = state.copy(mode = ServerSource.AUTOMATIC1111)
        // When
        val action = initial.toLocalModelDownloadActionState()
        // Then
        assertEquals(LocalModelDownloadActionState(false, false, false), action)
    }
}
