package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.work.fixture.IosGenerationFixture
import com.shifthackz.aisdv1.work.fixture.imagePayload
import com.shifthackz.aisdv1.work.fixture.textPayload
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
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
 * Covers terminal outcomes, retries and cancellation ownership across generation replacements.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("INVALID_CHARACTERS_NATIVE_ERROR")
class IosGenerationLifecycleTest {

    @Test
    fun `given replaced generation, its predecessor retains cancellation cleanup, expected no replacement admission until cleanup finishes`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            f.execution.readiness.values.single()()
            runCurrent()
            val cleanup = CompletableDeferred<Unit>()
            f.cleanupBarrier = cleanup
            // When
            f.manager.scheduleTextToImageTask(textPayload().copy(prompt = "replacement"))
            runCurrent()
            assertEquals(1, f.execution.readiness.size)
            cleanup.complete(Unit)
            runCurrent()
            // Then
            assertEquals(2, f.execution.readiness.size)
            assertEquals(1, f.textRequests.size)
        }

    @Test
    fun `given a cancellation chain, the waiting replacement is also replaced, expected the oldest cleanup barrier still gates dispatch`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            f.execution.readiness.values.single()()
            runCurrent()
            val cleanup = CompletableDeferred<Unit>()
            f.cleanupBarrier = cleanup
            f.manager.scheduleTextToImageTask(textPayload().copy(prompt = "middle"))
            runCurrent()
            // When
            f.manager.scheduleTextToImageTask(textPayload().copy(prompt = "last"))
            runCurrent()
            assertEquals(1, f.execution.readiness.size)
            cleanup.complete(Unit)
            runCurrent()
            f.execution.readiness.values.last()()
            runCurrent()
            // Then
            assertEquals(listOf("cat", "last"), f.textRequests.map { it.prompt })
            assertEquals(2, f.execution.readiness.size)
        }


    @Test
    fun `given admitted generation, persistence has not returned, expected native work remains running`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            f.execution.readiness.values.single()()
            runCurrent()
            // When
            f.observer.postProgressMessage("Saving", "Persisting", 100)
            runCurrent()
            // Then
            assertTrue(f.execution.outcomes.isEmpty())
            assertTrue(f.observer.hasActiveTasks())
            assertSame(BackgroundWorkResult.None, f.observer.observeResult().first())
        }

    @Test
    fun `given admitted generation, the use case returns persisted results, expected one successful terminal outcome`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            // When
            f.results.single().complete(emptyList())
            runCurrent()
            // Then
            assertEquals(mapOf(owner to true), f.execution.outcomes)
            assertIs<BackgroundWorkResult.Success>(f.observer.observeResult().first())
            assertFalse(f.observer.hasActiveTasks())
        }

    @Test
    fun `given admitted generation, the use case fails, expected the original failure without success`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            val failure = IllegalStateException("Cannot persist image")
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            f.execution.readiness.values.single()()
            runCurrent()
            // When
            f.results.single().completeExceptionally(failure)
            runCurrent()
            // Then
            assertSame(
                failure,
                assertIs<BackgroundWorkResult.Error>(f.observer.observeResult().first()).t
            )
            assertEquals(false, f.execution.outcomes.values.single())
            assertFalse(f.observer.hasActiveTasks())
        }

    @Test
    fun `given admitted Easy Diffusion generation, the system expires its execution grant, expected cancelled work without a wildcard interrupt`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            val owner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(owner)()
            runCurrent()
            // When
            f.execution.expirations.getValue(owner)()
            runCurrent()
            f.results.single().complete(emptyList())
            runCurrent()
            // Then
            assertEquals(false, f.execution.outcomes[owner])
            assertSame(BackgroundWorkResult.None, f.observer.observeResult().first())
            assertTrue(f.interruptions.isEmpty())
        }

    @Test
    fun `given a replacement generation, an old system expiration arrives, expected the replacement remains active`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.manager.scheduleTextToImageTask(textPayload())
            runCurrent()
            val firstOwner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(firstOwner)()
            runCurrent()
            f.manager.scheduleTextToImageTask(textPayload().copy(prompt = "replacement"))
            runCurrent()
            val replacement = f.execution.readiness.keys.last()
            f.execution.readiness.getValue(replacement)()
            runCurrent()
            // When
            f.execution.expirations.getValue(firstOwner)()
            f.results.first().complete(emptyList())
            runCurrent()
            // Then
            assertTrue(f.observer.hasActiveTasks())
            assertFalse(f.execution.outcomes.containsKey(replacement))
            assertEquals("replacement", f.textRequests.last().prompt)
            assertSame(BackgroundWorkResult.None, f.observer.observeResult().first())
        }

    @Test
    fun `given a failed text request, retry follows a settings change, expected the captured payload and a fresh native owner`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            val payload = textPayload()
            f.manager.scheduleTextToImageTask(payload)
            runCurrent()
            val firstOwner = f.execution.readiness.keys.single()
            f.execution.readiness.getValue(firstOwner)()
            runCurrent()
            f.results.single().completeExceptionally(IllegalStateException("offline"))
            runCurrent()
            f.preferences.source = ServerSource.LOCAL_APPLE_BONSAI
            // When
            assertTrue(f.manager.retryLastTextToImageTask().isSuccess)
            runCurrent()
            val retryOwner = f.execution.readiness.keys.last()
            f.execution.readiness.getValue(retryOwner)()
            runCurrent()
            // Then
            assertTrue(firstOwner != retryOwner)
            assertEquals(listOf(payload, payload), f.textRequests)
            assertEquals(false, f.execution.gpuRequirements[retryOwner])
        }

    @Test
    fun `given a failed image request, retry is selected, expected the original image and mask remain captured`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            val payload = imagePayload().copy(base64MaskImage = "mask")
            f.manager.scheduleImageToImageTask(payload)
            runCurrent()
            f.execution.readiness.values.single()()
            runCurrent()
            f.results.single().completeExceptionally(IllegalStateException("offline"))
            runCurrent()
            // When
            assertTrue(f.manager.retryLastImageToImageTask().isSuccess)
            runCurrent()
            f.execution.readiness.values.last()()
            runCurrent()
            // Then
            assertEquals(listOf(payload, payload), f.imageRequests)
        }

    @Test
    fun `given an empty generation history, either retry is selected, expected a failure without native work`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            // When
            val text = f.manager.retryLastTextToImageTask()
            val image = f.manager.retryLastImageToImageTask()
            runCurrent()
            // Then
            assertTrue(text.isFailure)
            assertTrue(image.isFailure)
            assertTrue(f.execution.readiness.isEmpty())
        }

    @Test
    fun `given a running legacy provider, cancel is requested twice, expected one interruption after dispatch`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.preferences.source = ServerSource.AUTOMATIC1111
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.AUTOMATIC1111))
            runCurrent()
            f.execution.readiness.values.single()()
            runCurrent()
            // When
            f.manager.cancelAll()
            f.manager.cancelAll()
            runCurrent()
            // Then
            assertEquals(listOf(ServerSource.AUTOMATIC1111), f.interruptions)
        }

    @Test
    fun `given a running legacy provider, settings select another provider before cancel, expected no interruption of the newly selected provider`() =
        runTest {
            // Given
            val f = IosGenerationFixture(backgroundScope)
            f.preferences.source = ServerSource.AUTOMATIC1111
            f.manager.scheduleTextToImageTask(textPayload(ServerSource.AUTOMATIC1111))
            runCurrent()
            f.execution.readiness.values.single()()
            runCurrent()
            // When
            f.preferences.source = ServerSource.SWARM_UI
            f.manager.cancelAll()
            runCurrent()
            // Then
            assertTrue(f.interruptions.isEmpty())
            assertFalse(f.observer.hasActiveTasks())
        }
}
