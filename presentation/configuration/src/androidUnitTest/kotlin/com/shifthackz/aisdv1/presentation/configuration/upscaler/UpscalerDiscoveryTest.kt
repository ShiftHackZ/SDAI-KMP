package com.shifthackz.aisdv1.presentation.configuration.upscaler

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.UpscalerFeatureTag
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Protects platform visibility and independent capability/search intersections in provider discovery.
 *
 * @author Dmitriy Moroz
 */
class UpscalerDiscoveryTest {
    @Test
    fun `given iOS upscaler discovery, sources are requested, expected only platform allowed MNN provider`() {
        // Given
        val state = UpscalerSetupState(Platform.IOS)
        // When
        val sources = state.sources
        // Then
        assertEquals(listOf(UpscalerSource.LOCAL_MNN), sources)
    }

    @Test
    fun `given upscaler search, mixed case provider and version terms are entered, expected matching MNN provider`() {
        // Given
        val state = UpscalerSetupState(
            Platform.ANDROID,
            discovery = UpscalerDiscoveryState(query = "MnN 2026.11.24")
        )
        // When
        val sources = state.visibleSources(ServerSetupStrings())
        // Then
        assertEquals(listOf(UpscalerSource.LOCAL_MNN), sources)
    }

    @Test
    fun `given capability filters, multiple models and tiled inference are required, expected ESRGAN provider only`() {
        // Given
        val state = UpscalerSetupState(
            Platform.ANDROID, discovery = UpscalerDiscoveryState(
                tags = setOf(UpscalerFeatureTag.MultipleModels, UpscalerFeatureTag.TiledInference)
            )
        )
        // When
        val sources = state.visibleSources(ServerSetupStrings())
        // Then
        assertEquals(listOf(UpscalerSource.LOCAL_ESRGAN), sources)
    }

    @Test
    fun `given local upscaler discovery, cloud hosting is evaluated, expected impossible filter disabled`() {
        // Given
        val state = UpscalerSetupState(Platform.ANDROID)
        // When
        val canSelectCloud = state.sources.any {
            state.matches(it, ServerSourceType.CLOUD, emptySet(), emptySet())
        }
        // Then
        assertFalse(canSelectCloud)
    }
}
