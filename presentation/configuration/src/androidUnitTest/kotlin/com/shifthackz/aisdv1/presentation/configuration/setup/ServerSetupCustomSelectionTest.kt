package com.shifthackz.aisdv1.presentation.configuration.setup

import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.configuration.setup.component.mainButtonEnabled
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Keeps explicit custom toggles authoritative when saved selections arrive from a late catalog refresh.
 *
 * @param source Active provider whose standard custom switch is changed.
 * @param custom Standard custom identity for this provider.
 *
 * @author Dmitriy Moroz
 */
@RunWith(Parameterized::class)
class ServerSetupCustomSelectionTest(
    private val source: ServerSource,
    private val custom: LocalAiModel
) {
    @Test
    fun `given custom model toggle, user disables saved custom during refresh, expected explicit deselection retained`() {
        // Given
        val saved = custom.copy(selected = true)
        val initial = ServerSetupState(mode = source).withCatalogResult(
            custom.type,
            listOf(saved),
            LocalCatalogStatus.READY
        )

        // When
        val refreshed = initial.withAllowCustomModel(false)
            .withCatalogResult(custom.type, listOf(saved), LocalCatalogStatus.READY)

        // Then
        assertEquals(false, refreshed.catalogModels(custom.type).single().selected)
    }

    @Test
    fun `given custom model toggle, user enables custom during refresh, expected explicit custom choice retained`() {
        // Given
        val initial = ServerSetupState(mode = source).withCatalogResult(
            custom.type,
            listOf(custom),
            LocalCatalogStatus.READY
        )

        // When
        val refreshed = initial.withAllowCustomModel(true)
            .withCatalogResult(custom.type, listOf(custom), LocalCatalogStatus.READY)

        // Then
        assertEquals(true, refreshed.catalogModels(custom.type).single().selected)
    }

    @Test
    fun `given custom model enabled, user selects undownloaded catalog model, expected setup disabled`() {
        // Given
        val catalog = custom.copy(id = "catalog-model", selected = false, downloaded = false)
        val initial = ServerSetupState(mode = source, step = ServerSetupState.Step.CONFIGURE)
            .withCatalogResult(custom.type, listOf(custom, catalog), LocalCatalogStatus.READY)
            .withAllowCustomModel(true)

        // When
        val selected = initial.withSelectedLocalModel(
            initial.catalogModels(custom.type).first { it.id == catalog.id })

        // Then
        assertEquals(true, selected.localCustomModel)
        assertEquals(
            true,
            selected.catalogModels(custom.type).first { it.id == catalog.id }.selected
        )
        assertEquals(false, selected.mainButtonEnabled)
    }

    @Test
    fun `given custom model enabled, user selects downloaded catalog model, expected setup enabled`() {
        // Given
        val catalog = custom.copy(id = "catalog-model", selected = false, downloaded = true)
        val initial = ServerSetupState(mode = source, step = ServerSetupState.Step.CONFIGURE)
            .withCatalogResult(custom.type, listOf(custom, catalog), LocalCatalogStatus.READY)
            .withAllowCustomModel(true)

        // When
        val selected = initial.withSelectedLocalModel(
            initial.catalogModels(custom.type).first { it.id == catalog.id })

        // Then
        assertEquals(true, selected.mainButtonEnabled)
    }

    @Test
    fun `given custom model enabled, user selects custom entry, expected setup enabled without catalog download`() {
        // Given
        val initial = ServerSetupState(mode = source, step = ServerSetupState.Step.CONFIGURE)
            .withCatalogResult(
                custom.type,
                listOf(custom.copy(downloaded = false)),
                LocalCatalogStatus.READY
            )

        // When
        val selected = initial.withAllowCustomModel(true)

        // Then
        assertEquals(true, selected.mainButtonEnabled)
    }

    @Test
    fun `given custom model disabled, no model is selected, expected setup disabled`() {
        // Given
        val initial = ServerSetupState(mode = source, step = ServerSetupState.Step.CONFIGURE)
            .withCatalogResult(custom.type, listOf(custom), LocalCatalogStatus.READY)
            .withAllowCustomModel(true)

        // When
        val selected = initial.withAllowCustomModel(false)

        // Then
        assertEquals(false, selected.mainButtonEnabled)
    }

    companion object {
        /**
         * Selects provider forms that expose the shared standard custom-model switch.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun catalogs(): List<Array<Any>> = listOf(
            arrayOf(ServerSource.LOCAL_MNN, LocalAiModel.CustomMnn),
            arrayOf(ServerSource.LOCAL_MICROSOFT_ONNX, LocalAiModel.CustomOnnx),
            arrayOf(ServerSource.LOCAL_GOOGLE_MEDIA_PIPE, LocalAiModel.CustomMediaPipe),
            arrayOf(ServerSource.LOCAL_STABLE_DIFFUSION_CPP, LocalAiModel.CustomSdxl)
        )
    }
}
