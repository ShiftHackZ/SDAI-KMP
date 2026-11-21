package com.shifthackz.aisdv1.data.mappers

import com.shifthackz.aisdv1.data.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultMetadataEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Protects provenance across full image rows and lightweight gallery queries.
 *
 * @author Dmitriy Moroz
 */
class GenerationProvenanceMappersTest {
    @Test
    fun `given generated image provenance, storage row round trips, expected all metadata and image parameters are preserved`() {
        // Given
        val image = mockAiGenerationResult.copy(
            providerKey = "local_mnn",
            acceleration = "OpenCL",
            generationDurationMillis = 3_662_123,
            deviceName = "Google Pixel 7 Pro",
            osVersion = "Android 17 (API 37)"
        )
        // When
        val restored = image.mapDomainToEntity().mapEntityToDomain()
        // Then
        assertEquals(image, restored)
    }

    @Test
    fun `given legacy gallery image, storage row round trips, expected missing provenance remains unknown`() {
        // Given
        val image = mockAiGenerationResult
        // When
        val restored = image.mapDomainToEntity().mapEntityToDomain()
        // Then
        assertEquals(image, restored)
        assertNull(restored.providerKey)
        assertNull(restored.acceleration)
        assertNull(restored.generationDurationMillis)
        assertNull(restored.deviceName)
        assertNull(restored.osVersion)
    }

    @Test
    fun `given lightweight gallery projection, metadata is mapped without originals, expected generation provenance reaches details unchanged`() {
        // Given
        val row = with(mockAiGenerationResult) {
            GenerationResultMetadataEntity(
                id,
                imageRevision,
                createdAt,
                type.key,
                prompt,
                negativePrompt,
                width,
                height,
                samplingSteps,
                cfgScale,
                restoreFaces,
                sampler,
                seed,
                subSeed,
                subSeedStrength,
                denoisingStrength,
                hidden,
                liked,
                modelName,
                providerKey = "local_mnn",
                acceleration = "CPU",
                generationDurationMillis = 0,
                deviceName = "Pixel 7 Pro",
                osVersion = "Android 17 (API 37)"
            )
        }
        // When
        val metadata = row.mapMetadataToDomain()
        // Then
        assertEquals(row.providerKey, metadata.providerKey)
        assertEquals(row.acceleration, metadata.acceleration)
        assertEquals(row.generationDurationMillis, metadata.generationDurationMillis)
        assertEquals(row.deviceName, metadata.deviceName)
        assertEquals(row.osVersion, metadata.osVersion)
        assertEquals(row.imageRevision, metadata.imageRevision)
    }
}
