package com.shifthackz.aisdv1.domain.entity

import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResult
import kotlin.time.TestTimeSource
import kotlin.time.Duration.Companion.milliseconds
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.test.runTest

class GenerationMetadataContextTest {
    @Test
    fun `given image generation provenance, completion is captured before persistence, expected elapsed duration and unchanged image guards`() =
        runTest {
            // Given
            val clock = TestTimeSource()
            val metadata = GenerationMetadataContext(
                "local_mnn",
                "OpenCL",
                started = clock.markNow(),
                deviceName = "Google Pixel 7 Pro",
                osVersion = "Android 17 (API 37)"
            )
            val result = mockAiGenerationResult.copy(providerSelectionRevision = 42)
            clock += 3_662_123.milliseconds
            // When
            val completed = metadata.completed(result)
            clock += 10_000.milliseconds
            val nextBatchImage = metadata.completed(result.copy(id = 999))
            // Then
            assertEquals(
                completed.generationDurationMillis,
                nextBatchImage.generationDurationMillis
            )
            assertEquals(
                result.copy(
                    deviceName = "Google Pixel 7 Pro",
                    osVersion = "Android 17 (API 37)",
                    providerKey = "local_mnn",
                    acceleration = "OpenCL",
                    generationDurationMillis = 3_662_123
                ), completed
            )
        }

    @Test
    fun `given remote generation provenance, image completes without local acceleration, expected None mode and zero duration retained`() =
        runTest {
            // Given
            val clock = TestTimeSource()
            val metadata = GenerationMetadataContext("open_ai", "None", started = clock.markNow())
            // When
            val completed = metadata.completed(mockAiGenerationResult)
            // Then
            assertEquals("open_ai", completed.providerKey)
            assertEquals("None", completed.acceleration)
            assertEquals(0L, completed.generationDurationMillis)
        }

    @Test
    fun `given a result reports a resolved backend, completion adds provenance, expected reported acceleration takes precedence over automatic selection`() =
        runTest {
            // Given
            val metadata = GenerationMetadataContext("local_stable_diffusion_cpp", "AUTO")
            val result = mockAiGenerationResult.copy(acceleration = "CPU")
            // When
            val completed = metadata.completed(result)
            // Then
            assertEquals("CPU", completed.acceleration)
            assertEquals("local_stable_diffusion_cpp", completed.providerKey)
        }

}
