package com.shifthackz.aisdv1.data.core

import com.shifthackz.aisdv1.data.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.GenerationMetadataContext
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

/**
 * Verifies metadata reaches the application's normal persistence boundary before any gallery writes.
 *
 * @author Dmitriy Moroz
 */
class GenerationMetadataPersistenceTest {
    private val preferences = mockk<PreferenceManager>(relaxed = true)
    private val storage = mockk<GenerationResultDataSource.Local>()
    private val work = mockk<BackgroundWorkObserver>(relaxed = true)
    private val repository = object : CoreGenerationRepository(
        mockk<MediaStoreGateway>(), storage, preferences, work
    ) {
        /**
         * Invokes the production completion path without replacing its persistence policy.
         *
         * @param result Completed provider image.
         * @author Dmitriy Moroz
         */
        suspend fun complete(result: AiGenerationResult) = insertGenerationResult(result)
    }

    @Test
    fun `given foreground generation without autosave, image completes, expected metadata returned without a gallery write`() =
        runTest {
            // Given
            val clock = TestTimeSource()
            val metadata = GenerationMetadataContext(
                "local_mnn",
                "CPU",
                "Pixel 7 Pro",
                "Android 17 (API 37)",
                clock.markNow()
            )
            clock += 15.seconds
            // When
            val result = withContext(metadata) { repository.complete(mockAiGenerationResult) }
            // Then
            assertEquals(
                mockAiGenerationResult.copy(
                    providerKey = "local_mnn",
                    acceleration = "CPU",
                    deviceName = "Pixel 7 Pro",
                    osVersion = "Android 17 (API 37)",
                    generationDurationMillis = 15_000
                ), result
            )
            coVerify(exactly = 0) { storage.insert(any()) }
        }

    @Test
    fun `given foreground autosave, gallery insertion takes additional time, expected saved and returned metadata exclude persistence time`() =
        runTest {
            // Given
            every { preferences.autoSaveAiResults } returns true
            val clock = TestTimeSource()
            val metadata =
                GenerationMetadataContext("local_mnn", "OpenCL", started = clock.markNow())
            val saved = slot<AiGenerationResult>()
            coEvery { storage.insert(capture(saved)) } coAnswers { clock += 20.seconds; 42L }
            clock += 5.seconds
            // When
            val result = withContext(metadata) { repository.complete(mockAiGenerationResult) }
            // Then
            assertEquals(5_000L, saved.captured.generationDurationMillis)
            assertEquals("local_mnn", saved.captured.providerKey)
            assertEquals("OpenCL", saved.captured.acceleration)
            assertEquals(saved.captured.copy(id = 42), result)
        }

    @Test
    fun `given background generation without foreground autosave, image completes, expected provenance persisted with the work result`() =
        runTest {
            // Given
            every { work.hasActiveTasks() } returns true
            val saved = slot<AiGenerationResult>()
            coEvery { storage.insert(capture(saved)) } returns 43
            // When
            val result = withContext(GenerationMetadataContext("open_ai", "None")) {
                repository.complete(mockAiGenerationResult)
            }
            // Then
            assertEquals("open_ai", saved.captured.providerKey)
            assertEquals("None", saved.captured.acceleration)
            assertEquals(saved.captured.copy(id = 43), result)
        }

    @Test
    fun `given a native batch with autosave, saving the first image advances time, expected subsequent images retain the original batch duration`() =
        runTest {
            // Given
            every { preferences.autoSaveAiResults } returns true
            val clock = TestTimeSource()
            val metadata = GenerationMetadataContext("custom", "None", started = clock.markNow())
            coEvery { storage.insert(any()) } coAnswers { clock += 30.seconds; 1L }
            clock += 10.seconds
            // When
            val images =
                withContext(metadata) { List(2) { repository.complete(mockAiGenerationResult) } }
            // Then
            assertEquals(listOf(10_000L, 10_000L), images.map { it.generationDurationMillis })
            coVerify(exactly = 2) { storage.insert(any()) }
        }

    @Test
    fun `given a result outside active generation, an existing image is saved, expected its historical metadata is preserved`() =
        runTest {
            // Given
            val original = mockAiGenerationResult.copy(
                providerKey = "local_mnn",
                acceleration = "Metal",
                deviceName = "iPhone 17 Pro",
                osVersion = "iOS 26",
                generationDurationMillis = 1234
            )
            // When
            val result = repository.complete(original)
            // Then
            assertSame(original, result)
            coVerify(exactly = 0) { storage.insert(any()) }
        }
}
