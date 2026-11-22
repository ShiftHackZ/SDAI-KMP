package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgress
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.work.fixture.IosGenerationFixture
import com.shifthackz.aisdv1.work.fixture.textPayload
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Keeps measured progress correlated with the captured provider and work owner.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("INVALID_CHARACTERS_NATIVE_ERROR")
class IosGenerationProgressTest {

    @Test
    fun `given remote generation, unrelated owners and providers report progress, expected only the matching measurement reaches native work`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.AUTOMATIC1111))
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            // When
            f.remote.value = listOf(
                RemoteGenerationProgress(ServerSource.AUTOMATIC1111, "unrelated", 90),
                RemoteGenerationProgress(ServerSource.SWARM_UI, owner, 80),
                RemoteGenerationProgress(ServerSource.AUTOMATIC1111, owner, 35),
            )
            runCurrent()
            // Then
            assertEquals(35, f.execution.progress[owner])
        }

    @Test
    fun `given remote generation, its measured submission disappears, expected indeterminate native progress`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.SWARM_UI))
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            f.remote.value = listOf(RemoteGenerationProgress(ServerSource.SWARM_UI, owner, 50))
            runCurrent()
            // When
            f.remote.value = emptyList()
            runCurrent()
            // Then
            assertNull(f.execution.progress[owner])
        }

    @Test
    fun `given Easy Diffusion generation, another owner or an inactive operation reports steps, expected no leaked measurement`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            // When
            f.easy.emit(EasyDiffusionProgress(18, 20, true, "foreground"))
            f.easy.emit(EasyDiffusionProgress(18, 20, false, owner))
            runCurrent()
            // Then
            assertNull(f.execution.progress[owner])
        }

    @Test
    fun `given Easy Diffusion generation, matching steps transition into an unknown stage, expected measured progress then an indeterminate stage`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            // When
            f.easy.emit(EasyDiffusionProgress(5, 20, true, owner))
            runCurrent()
            assertEquals(25, f.execution.progress[owner])
            f.easy.emit(EasyDiffusionProgress(0, 0, true, owner))
            runCurrent()
            // Then
            assertNull(f.execution.progress[owner])
        }

    @Test
    fun `given MNN CPU generation, overall percentage exceeds its bounds, expected bounded progress without denoising ratios`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.LOCAL_MNN))
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            // When
            f.local.emit(LocalDiffusionStatus(125, 100))
            runCurrent()
            // Then
            assertEquals(100, f.execution.progress[owner])
            assertFalse(f.execution.outcomes.containsKey(owner))
        }

    @Test
    fun `given Core ML generation, the total is unknown then measured, expected indeterminate progress followed by a valid step ratio`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.LOCAL_APPLE_CORE_ML))
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            // When
            f.local.emit(LocalDiffusionStatus(1, 0))
            runCurrent()
            assertNull(f.execution.progress[owner])
            f.local.emit(LocalDiffusionStatus(4, 20))
            runCurrent()
            // Then
            assertEquals(20, f.execution.progress[owner])
        }

    @Test
    fun `given completed generation, a delayed remote measurement arrives, expected no resumed native progress`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.AUTOMATIC1111))
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            f.results.single().complete(emptyList())
            runCurrent()
            // When
            f.remote.value = listOf(RemoteGenerationProgress(ServerSource.AUTOMATIC1111, owner, 70))
            runCurrent()
            // Then
            assertNull(f.execution.progress[owner])
            assertEquals(true, f.execution.outcomes[owner])
        }
}
