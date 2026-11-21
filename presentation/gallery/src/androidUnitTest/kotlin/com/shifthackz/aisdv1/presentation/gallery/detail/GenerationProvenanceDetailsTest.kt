package com.shifthackz.aisdv1.presentation.gallery.detail

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.gallery.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.presentation.gallery.mocks.toGalleryMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies persisted and transient details expose identical provenance and export it in reading order.
 *
 * @author Dmitriy Moroz
 */
class GenerationProvenanceDetailsTest {
    @Before
    fun initializeLocalization() {
        Localization.setLanguageCode("en")
    }

    @Test
    fun `given generated image provenance, transient and persisted details are opened, expected matching formatted metadata`() {
        // Given
        val result = mockAiGenerationResult.copy(
            providerKey = "local_mnn",
            modelName = "SD 1.5",
            acceleration = "OpenCL",
            generationDurationMillis = 3_662_000,
            deviceName = "Google Pixel 7 Pro",
            osVersion = "Android 17 (API 37)"
        )
        // When
        val transient = result.toGalleryDetailContent(false)
        val persisted = result.toGalleryMetadata().toGalleryDetailContent(false)
        // Then
        assertEquals("01:01:02", persisted.generationDuration)
        assertEquals(transient.provider, persisted.provider)
        assertEquals("OpenCL", persisted.acceleration)
        assertEquals(result.deviceName, persisted.deviceName)
        assertEquals(result.osVersion, persisted.osVersion)
        assertEquals(transient.paramsText(), persisted.paramsText())
    }

    @Test
    fun `given metadata text export, image parameters are shared, expected model immediately follows provider and device data is present`() {
        // Given
        val result = mockAiGenerationResult.copy(
            providerKey = "local_mnn",
            modelName = "SD 1.5",
            acceleration = "CPU",
            generationDurationMillis = 15_000,
            deviceName = "Pixel 7 Pro",
            osVersion = "Android 17 (API 37)"
        )
        // When
        val lines = result.toGalleryDetailContent(false).paramsText().lines()
        // Then
        val providerIndex = lines.indexOfFirst { it.startsWith("Provider:") }
        assertTrue(providerIndex >= 0)
        assertEquals("Model: SD 1.5", lines[providerIndex + 1])
        assertTrue(lines.contains("Acceleration: CPU"))
        assertTrue(lines.contains("Generation time: 00:15"))
        assertTrue(lines.contains("Device: Pixel 7 Pro"))
        assertTrue(lines.contains("OS: Android 17 (API 37)"))
    }

    @Test
    fun `given legacy gallery image, details and parameter export are opened, expected unknown metadata is not fabricated`() {
        // Given
        val legacy = mockAiGenerationResult
        // When
        val content = legacy.toGalleryDetailContent(false)
        val text = content.paramsText()
        // Then
        assertNull(content.provider)
        assertNull(content.acceleration)
        assertNull(content.generationDuration)
        assertNull(content.deviceName)
        assertNull(content.osVersion)
        assertTrue(
            text.lines().none {
                it.startsWith("Provider:") || it.startsWith("Generation time:") || it.startsWith("Device:") || it.startsWith(
                    "OS:"
                )
            })
    }

    @Test
    fun `given remote image with zero duration, details are formatted, expected None and zero time remain explicit values`() {
        // Given
        val image = mockAiGenerationResult.copy(
            providerKey = "open_ai",
            acceleration = "None",
            generationDurationMillis = 0
        )
        // When
        val content = image.toGalleryDetailContent(false)
        // Then
        assertEquals("None", content.acceleration)
        assertEquals("00:00", content.generationDuration)
    }

    @Test
    fun `given imported provenance from a future provider, details are formatted, expected provider key and execution mode remain readable`() {
        // Given
        val result = mockAiGenerationResult.copy(
            providerKey = "future_provider",
            acceleration = "Future engine"
        )
        // When
        val transient = result.toGalleryDetailContent(false)
        val persisted = result.toGalleryMetadata().toGalleryDetailContent(false)
        // Then
        assertEquals("future_provider", transient.provider)
        assertEquals("Future engine", persisted.acceleration)
        assertEquals(transient.provider, persisted.provider)
    }

}
