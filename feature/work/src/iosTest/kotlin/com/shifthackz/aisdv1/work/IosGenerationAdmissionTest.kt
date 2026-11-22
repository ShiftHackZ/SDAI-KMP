package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.work.fixture.IosGenerationFixture
import com.shifthackz.aisdv1.work.fixture.imagePayload
import com.shifthackz.aisdv1.work.fixture.textPayload
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Verifies native admission precedes dispatch and resource requirements follow captured inputs.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("INVALID_CHARACTERS_NATIVE_ERROR")
class IosGenerationAdmissionTest {

    @Test
    fun `given text generation, native readiness is delayed, expected no inference before admission`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            val payload = textPayload()
            f.manager.scheduleTextToImageTask(payload)
            runCurrent()
            // When
            val owner = f.execution.readiness.keys.single()
            assertTrue(f.textRequests.isEmpty())
            assertFalse(f.observer.hasActiveTasks())
            f.execution.readiness.getValue(owner)()
            runCurrent()
            // Then
            assertEquals(listOf(payload), f.textRequests)
            assertTrue(f.observer.hasActiveTasks())
        }

    @Test
    fun `given image generation, native readiness is delayed, expected the original image is dispatched only after admission`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            val payload = imagePayload()
            f.manager.scheduleImageToImageTask(payload)
            runCurrent()
            // When
            assertTrue(f.imageRequests.isEmpty())
            f.execution.readiness.values.single()()
            runCurrent()
            // Then
            assertEquals(listOf(payload), f.imageRequests)
            assertEquals(false, f.execution.gpuRequirements.values.single())
        }

    @Test
    fun `given GPU generation, native admission is unavailable, expected a failure without engine dispatch`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.LOCAL_APPLE_BONSAI))
            runCurrent()
            // When
            val owner = f.execution.readiness.keys.single()
            f.execution.rejections.getValue(owner)()
            runCurrent()
            // Then
            assertTrue(f.textRequests.isEmpty())
            assertFalse(f.observer.hasActiveTasks())
            assertIs<BackgroundWorkResult.Error>(f.observer.observeResult().first())
            assertEquals(false, f.execution.outcomes[owner])
            assertTrue(f.interruptions.isEmpty())
        }

    @Test
    fun `given pending generation, cancel precedes a late native readiness callback, expected no inference or result`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            // When
            f.manager.cancelAll()
            runCurrent()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            // Then
            assertTrue(f.textRequests.isEmpty())
            assertEquals(false, f.execution.outcomes[owner])
            assertSame(BackgroundWorkResult.None, f.observer.observeResult().first())
        }

    @Test
    fun `given MNN CPU generation, settings select another provider before admission, expected CPU resource requirements from the captured request`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.preferences.source = ServerSource.LOCAL_APPLE_BONSAI
            // When
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.LOCAL_MNN, MnnBackend.CPU))
            runCurrent()
            // Then
            assertEquals(false, f.execution.gpuRequirements.values.single())
        }

    @Test
    fun `given MNN Metal generation, native execution is requested, expected a GPU resource requirement`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            // When
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.LOCAL_MNN, MnnBackend.METAL))
            runCurrent()
            // Then
            assertEquals(true, f.execution.gpuRequirements.values.single())
        }

    @Test
    fun `given Core ML image generation, native execution is requested, expected a GPU resource requirement`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            // When
            f.manager.scheduleImageToImageTask(imagePayload(ServerSource.LOCAL_APPLE_CORE_ML))
            runCurrent()
            // Then
            assertEquals(true, f.execution.gpuRequirements.values.single())
        }

    @Test
    fun `given legacy generation, the payload has no source snapshot, expected resource requirements from the selected provider`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.preferences.source = ServerSource.LOCAL_APPLE_CORE_ML
            // When
            f.manager.scheduleTextToImageTask(textPayload(null))
            runCurrent()
            // Then
            assertEquals(true, f.execution.gpuRequirements.values.single())
        }
}
