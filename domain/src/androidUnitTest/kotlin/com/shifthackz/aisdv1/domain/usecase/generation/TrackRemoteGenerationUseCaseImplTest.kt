package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionWorkOwner
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgress
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgressContext
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.RemoteGenerationProgressGateway
import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.repository.Automatic1111ProgressRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.currentCoroutineContext

/**
 * Verifies optional telemetry against the production generation lifetime, including cancellation and owner isolation.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrackRemoteGenerationUseCaseImplTest {
    private val updates = mutableListOf<Pair<String, RemoteGenerationProgress?>>()
    private val gateway = mockk<RemoteGenerationProgressGateway> {
        every {
            update(
                any(),
                any()
            )
        } answers { updates.add(firstArg<String>() to secondArg()); Unit }
    }
    private val repository = mockk<Automatic1111ProgressRepository>()
    private val useCase = TrackRemoteGenerationUseCaseImpl(gateway, repository)

    @Test
    fun `given remote generation, generation succeeds, expected original results and progress cleanup`() =
        runTest {
            // Given
            val expected = listOf(mockAiGenerationResult)
            // When
            val result = useCase(ServerSource.SWARM_UI) {
                assertEquals(ServerSource.SWARM_UI, updates.single().second?.source)
                assertNull(updates.single().second?.percentage)
                expected
            }
            // Then
            assertSame(expected, result)
            assertNull(updates.last().second)
            assertEquals(updates.first().first, updates.last().first)
            coVerify(exactly = 0) { repository.getProgress(any()) }
        }

    @Test
    fun `given remote generation, fractional measurement arrives, expected bounded percentage 42`() =
        runTest {
            // Given
            var actual: Int? = -1
            // When
            useCase(ServerSource.SWARM_UI) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(0.429f)
                actual = updates.last().second?.percentage
                emptyList()
            }
            // Then
            assertEquals(42, actual)
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, zero measurement arrives, expected bounded percentage 0`() =
        runTest {
            // Given
            var actual: Int? = -1
            // When
            useCase(ServerSource.SWARM_UI) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(0f)
                actual = updates.last().second?.percentage
                emptyList()
            }
            // Then
            assertEquals(0, actual)
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, complete measurement arrives, expected bounded percentage 100`() =
        runTest {
            // Given
            var actual: Int? = -1
            // When
            useCase(ServerSource.SWARM_UI) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(1f)
                actual = updates.last().second?.percentage
                emptyList()
            }
            // Then
            assertEquals(100, actual)
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, negative measurement arrives, expected bounded percentage 0`() =
        runTest {
            // Given
            var actual: Int? = -1
            // When
            useCase(ServerSource.SWARM_UI) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(-0.1f)
                actual = updates.last().second?.percentage
                emptyList()
            }
            // Then
            assertEquals(0, actual)
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, excess measurement arrives, expected bounded percentage 100`() =
        runTest {
            // Given
            var actual: Int? = -1
            // When
            useCase(ServerSource.SWARM_UI) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(1.5f)
                actual = updates.last().second?.percentage
                emptyList()
            }
            // Then
            assertEquals(100, actual)
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, NaN measurement arrives, expected bounded percentage null`() =
        runTest {
            // Given
            var actual: Int? = -1
            // When
            useCase(ServerSource.SWARM_UI) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(Float.NaN)
                actual = updates.last().second?.percentage
                emptyList()
            }
            // Then
            assertEquals(null, actual)
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, positive infinity measurement arrives, expected bounded percentage null`() =
        runTest {
            // Given
            var actual: Int? = -1
            // When
            useCase(ServerSource.SWARM_UI) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(Float.POSITIVE_INFINITY)
                actual = updates.last().second?.percentage
                emptyList()
            }
            // Then
            assertEquals(null, actual)
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, negative infinity measurement arrives, expected bounded percentage null`() =
        runTest {
            // Given
            var actual: Int? = -1
            // When
            useCase(ServerSource.SWARM_UI) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(Float.NEGATIVE_INFINITY)
                actual = updates.last().second?.percentage
                emptyList()
            }
            // Then
            assertEquals(null, actual)
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, missing measurement arrives, expected bounded percentage null`() =
        runTest {
            // Given
            var actual: Int? = -1
            // When
            useCase(ServerSource.SWARM_UI) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(null)
                actual = updates.last().second?.percentage
                emptyList()
            }
            // Then
            assertEquals(null, actual)
            assertNull(updates.last().second)
        }

    @Test
    fun `given background remote generation, measurement arrives, expected captured worker ownership`() =
        runTest {
            // Given
            val owner = EasyDiffusionWorkOwner("worker-a")
            // When
            withContext(owner) {
                useCase(ServerSource.SWARM_UI) {
                    currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(0.5f)
                    emptyList()
                }
            }
            // Then
            assertTrue(updates.mapNotNull { it.second }.all { it.ownerId == "worker-a" })
            assertEquals(50, updates.mapNotNull { it.second }.last().percentage)
        }

    @Test
    fun `given foreground remote generation, measurement arrives, expected no background owner`() =
        runTest {
            // Given
            val source = ServerSource.SWARM_UI
            // When
            useCase(source) {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(0.5f)
                emptyList()
            }
            // Then
            assertTrue(updates.mapNotNull { it.second }.all { it.ownerId == null })
        }

    @Test
    fun `given AUTOMATIC1111 generation, operation remains active for two intervals, expected polling only after each interval`() =
        runTest {
            // Given
            coEvery { repository.getProgress(any()) } returns 0.4f
            // When
            val job = launch { useCase(ServerSource.AUTOMATIC1111) { delay(2500); emptyList() } }
            runCurrent()
            coVerify(exactly = 0) { repository.getProgress(any()) }
            advanceTimeBy(1000)
            runCurrent()
            coVerify(exactly = 1) { repository.getProgress(updates.first().first) }
            advanceTimeBy(1500)
            runCurrent()
            job.join()
            // Then
            coVerify(exactly = 2) { repository.getProgress(updates.first().first) }
            assertEquals(listOf(40, 40), updates.mapNotNull { it.second?.percentage })
            assertNull(updates.last().second)
        }

    @Test
    fun `given AUTOMATIC1111 generation, progress transport fails, expected unknown progress without retry or generation replay`() =
        runTest {
            // Given
            coEvery { repository.getProgress(any()) } throws IllegalStateException("telemetry unavailable")
            var generationCount = 0
            val expected = listOf(mockAiGenerationResult)
            // When
            val result = useCase(ServerSource.AUTOMATIC1111) {
                generationCount++
                delay(3500)
                expected
            }
            // Then
            assertSame(expected, result)
            assertEquals(1, generationCount)
            coVerify(exactly = 1) { repository.getProgress(any()) }
            assertTrue(updates.mapNotNull { it.second }.all { it.percentage == null })
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, provider generation fails, expected original failure and progress removal`() =
        runTest {
            // Given
            val failure = IllegalArgumentException("generation failed")
            // When
            val actual = runCatching { useCase(ServerSource.SWARM_UI) { throw failure } }
            // Then
            assertSame(failure, generateSequence(actual.exceptionOrNull()) { it.cause }.last())
            assertNull(updates.last().second)
        }

    @Test
    fun `given AUTOMATIC1111 generation, caller cancels active generation, expected polling stops and progress is removed`() =
        runTest {
            // Given
            coEvery { repository.getProgress(any()) } returns 0.4f
            var hasStarted = false
            val job = launch {
                useCase(ServerSource.AUTOMATIC1111) { hasStarted = true; awaitCancellation() }
            }
            runCurrent()
            // When
            job.cancelAndJoin()
            advanceTimeBy(5000)
            runCurrent()
            // Then
            assertTrue(hasStarted)
            assertTrue(job.isCancelled)
            assertNull(updates.last().second)
            coVerify(exactly = 0) { repository.getProgress(any()) }
        }

    @Test
    fun `given remote generation, provider reports cancellation, expected original cancellation and progress removal`() =
        runTest {
            // Given
            val failure = CancellationException("cancelled")
            // When
            val actual = runCatching { useCase(ServerSource.SWARM_UI) { throw failure } }
            // Then
            assertSame(failure, generateSequence(actual.exceptionOrNull()) { it.cause }.last())
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, callback arrives after completion, expected no resurrected measurement`() =
        runTest {
            // Given
            lateinit var context: RemoteGenerationProgressContext
            useCase(ServerSource.SWARM_UI) {
                context = currentCoroutineContext()[RemoteGenerationProgressContext]!!
                emptyList()
            }
            val count = updates.size
            // When
            context.report(0.9f)
            // Then
            assertEquals(count, updates.size)
            assertNull(updates.last().second)
        }

    @Test
    fun `given remote generation, consecutive submissions run, expected distinct task identities`() =
        runTest {
            // Given
            val ids = mutableListOf<String>()
            // When
            repeat(2) {
                useCase(ServerSource.SWARM_UI) {
                    ids += currentCoroutineContext()[RemoteGenerationProgressContext]!!.taskId
                    emptyList()
                }
            }
            // Then
            assertNotEquals(ids[0], ids[1])
            assertTrue(ids.all { it.startsWith("task(sdai-") })
        }

    @Test
    fun `given remote workers, one worker completes while another runs, expected independent owner identities and cleanup`() =
        runTest {
            // Given
            val release = CompletableDeferred<Unit>()
            val first = launch(EasyDiffusionWorkOwner("first")) {
                useCase(ServerSource.SWARM_UI) { release.await(); emptyList() }
            }
            runCurrent()
            val firstId = updates.first().first
            // When
            withContext(EasyDiffusionWorkOwner("second")) {
                useCase(ServerSource.SWARM_UI) { emptyList() }
            }
            // Then
            assertFalse(first.isCompleted)
            assertNotEquals(firstId, updates.last().first)
            assertEquals(listOf("first", "second"), updates.mapNotNull { it.second?.ownerId })
            release.complete(Unit)
            first.join()
            assertEquals(firstId to null, updates.last())
        }

    @Test
    fun `given AUTOMATIC1111 generation, later telemetry fails after valid progress, expected measurement cleared while generation completes`() =
        runTest {
            // Given
            coEvery { repository.getProgress(any()) } returns 0.75f andThenThrows IllegalStateException(
                "offline"
            )
            // When
            val result =
                useCase(ServerSource.AUTOMATIC1111) { delay(3500); listOf(mockAiGenerationResult) }
            // Then
            assertEquals(listOf(mockAiGenerationResult), result)
            assertEquals(
                listOf(null, 75, null),
                updates.mapNotNull { it.second }.map { it.percentage })
            assertNull(updates.last().second)
            coVerify(exactly = 2) { repository.getProgress(any()) }
        }

    @Test
    fun `given AUTOMATIC1111 generation, caller cancels during a telemetry request, expected in-flight request cancelled and progress removed`() =
        runTest {
            // Given
            val hasPolled = CompletableDeferred<Unit>()
            coEvery { repository.getProgress(any()) } coAnswers { hasPolled.complete(Unit); awaitCancellation() }
            val job = launch { useCase(ServerSource.AUTOMATIC1111) { awaitCancellation() } }
            runCurrent()
            advanceTimeBy(1000)
            runCurrent()
            hasPolled.await()
            // When
            job.cancelAndJoin()
            advanceTimeBy(5000)
            runCurrent()
            // Then
            assertTrue(job.isCancelled)
            assertNull(updates.last().second)
            coVerify(exactly = 1) { repository.getProgress(any()) }
        }

}
