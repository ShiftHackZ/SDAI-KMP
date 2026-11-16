package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionFrame
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTask
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionHttpFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionSubmissionFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTimeoutFailure
import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.mocks.assertRetainsFailure
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Verifies batching, recovery, shared deadlines and captured-task cleanup in production policy.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GenerateEasyDiffusionUseCaseImplTest {
    private val fixture = EasyDiffusionGenerationFixture()

    @Test
    fun `sequential batch wraps uint32 seeds and persists each completed image once`() = runTest {
        val requests = mutableListOf<EasyDiffusionRequest>()
        coEvery { fixture.repository.submit(capture(requests)) } returns fixture.task
        val result = fixture.useCase(fixture.request.copy(batchCount = 3))
        assertEquals(List(3) { mockAiGenerationResult }, result)
        assertEquals(listOf(4294967295L, 0L, 1L), requests.map { it.seed })
        assertTrue(requests.all { it.batchCount == 1 && it.sessionId == fixture.request.sessionId })
        coVerify(exactly = 3) { fixture.repository.save(any(), fixture.terminal) }
        coVerify(exactly = 0) { fixture.repository.stop(any()) }
        coVerify(exactly = 1) { fixture.registry.finish("session") }
    }

    @Test
    fun `second batch POST cannot start until first terminal result is saved`() = runTest {
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        var streams = 0
        coEvery { fixture.repository.readStream(any(), any()) } coAnswers {
            if (streams++ == 0) {
                firstEntered.complete(Unit)
                releaseFirst.await()
            }
            secondArg<suspend (EasyDiffusionFrame) -> Unit>()(fixture.terminal)
        }
        val generation = async { fixture.useCase(fixture.request.copy(batchCount = 2)) }
        firstEntered.await()
        coVerify(exactly = 1) { fixture.repository.submit(any()) }
        coVerify(exactly = 0) { fixture.repository.save(any(), any()) }
        releaseFirst.complete(Unit)
        assertEquals(2, generation.await().size)
        coVerifyOrder {
            fixture.repository.submit(any())
            fixture.repository.readStream(any(), any())
            fixture.repository.save(any(), any())
            fixture.repository.submit(any())
            fixture.repository.readStream(any(), any())
            fixture.repository.save(any(), any())
        }
    }

    @Test
    fun `batch deadline includes previous images and stops only unfinished second task`() =
        runTest {
            var submitted = 0L
            coEvery { fixture.repository.submit(any()) } coAnswers {
                fixture.task.copy(id = 42 + submitted++)
            }
            coEvery { fixture.repository.readStream(any(), any()) } coAnswers {
                delay(80000)
                secondArg<suspend (EasyDiffusionFrame) -> Unit>()(fixture.terminal)
            }
            assertTrue(runCatching { fixture.useCase(fixture.request.copy(batchCount = 2)) }
                .exceptionOrNull() is EasyDiffusionTimeoutFailure)
            assertEquals(120000L, testScheduler.currentTime)
            coVerify(exactly = 1) { fixture.repository.save(any(), any()) }
            coVerify(exactly = 1) { fixture.repository.stop(fixture.task.copy(id = 43)) }
            coVerify(exactly = 0) { fixture.repository.stop(fixture.task) }
        }

    @Test
    fun `pending 425 and empty streams reopen without replaying render`() = runTest {
        var attempt = 0
        coEvery { fixture.repository.readStream(any(), any()) } coAnswers {
            when (attempt++) {
                0 -> throw EasyDiffusionHttpFailure(425)
                1 -> Unit
                else -> secondArg<suspend (EasyDiffusionFrame) -> Unit>()(fixture.terminal)
            }
        }
        fixture.useCase(fixture.request)
        coVerify(exactly = 1) { fixture.repository.submit(any()) }
        coVerify(exactly = 3) { fixture.repository.readStream(fixture.task, any()) }
        coVerify(exactly = 2) { fixture.repository.ping(fixture.task.url, fixture.task.sessionId) }
        coVerify(exactly = 1) { fixture.repository.save(any(), any()) }
    }

    @Test
    fun `progress updates never save preview frames`() = runTest {
        val progress = EasyDiffusionFrame(step = 3, totalSteps = 20, image = "preview")
        coEvery { fixture.repository.readStream(any(), any()) } coAnswers {
            val consume = secondArg<suspend (EasyDiffusionFrame) -> Unit>()
            consume(progress)
            consume(fixture.terminal)
        }
        fixture.useCase(fixture.request)
        coVerify { fixture.registry.update("session", EasyDiffusionProgress(3, 20, true)) }
        coVerify(exactly = 1) { fixture.repository.save(fixture.request, fixture.terminal) }
    }

    @Test
    fun `missing checkpoint prevents submission and releases registry`() = runTest {
        coEvery { fixture.repository.getModels(any()) } returns emptyList()
        assertTrue(runCatching { fixture.useCase(fixture.request) }.exceptionOrNull() is IllegalStateException)
        coVerify(exactly = 0) { fixture.repository.submit(any()) }
        coVerify(exactly = 1) { fixture.registry.finish("session") }
    }

    @Test
    fun `ambiguous submit is not retried and never issues a global stop`() = runTest {
        val cause = IllegalStateException("connection lost after POST")
        coEvery { fixture.repository.submit(any()) } throws cause
        val failure = runCatching { fixture.useCase(fixture.request) }.exceptionOrNull()
        assertTrue(failure is EasyDiffusionSubmissionFailure)
        assertRetainsFailure(cause, failure)
        coVerify(exactly = 1) { fixture.repository.submit(any()) }
        coVerify(exactly = 0) { fixture.repository.stop(any()) }
        coVerify(exactly = 1) { fixture.registry.finish("session") }
    }

    @Test
    fun `nonrecoverable stream HTTP failure stops only accepted task and keeps original failure`() =
        runTest {
            val failure = EasyDiffusionHttpFailure(410)
            coEvery { fixture.repository.readStream(any(), any()) } throws failure
            coEvery { fixture.repository.stop(any()) } throws EasyDiffusionHttpFailure(409)
            assertSame(failure, runCatching { fixture.useCase(fixture.request) }.exceptionOrNull())
            coVerify(exactly = 1) { fixture.repository.stop(fixture.task) }
            coVerify(exactly = 0) { fixture.repository.save(any(), any()) }
        }

    @Test
    fun `failed stopped unknown and detailed frames cannot publish results`() = runTest {
        listOf(
            EasyDiffusionFrame(status = "failed"), EasyDiffusionFrame(status = "stopped"),
            EasyDiffusionFrame(status = "cancelled"), EasyDiffusionFrame(status = "unknown"),
            EasyDiffusionFrame(detail = "backend failed")
        ).forEach { frame ->
            val f = EasyDiffusionGenerationFixture()
            coEvery { f.repository.readStream(any(), any()) } coAnswers {
                secondArg<suspend (EasyDiffusionFrame) -> Unit>()(frame)
            }
            assertTrue(runCatching { f.useCase(f.request) }.isFailure)
            coVerify(exactly = 1) { f.repository.stop(f.task) }
            coVerify(exactly = 0) { f.repository.save(any(), any()) }
        }
    }

    @Test
    fun `data after terminal success is rejected before saving`() = runTest {
        coEvery { fixture.repository.readStream(any(), any()) } coAnswers {
            val consume = secondArg<suspend (EasyDiffusionFrame) -> Unit>()
            consume(fixture.terminal)
            consume(EasyDiffusionFrame(step = 1))
        }
        assertTrue(runCatching { fixture.useCase(fixture.request) }.isFailure)
        coVerify(exactly = 0) { fixture.repository.save(any(), any()) }
        coVerify { fixture.repository.stop(fixture.task) }
    }

    @Test
    fun `shared generation timeout bounds a pending task and performs captured cleanup`() =
        runTest {
            coEvery {
                fixture.repository.readStream(
                    any(),
                    any()
                )
            } coAnswers { awaitCancellation() }
            assertTrue(runCatching { fixture.useCase(fixture.request) }.exceptionOrNull() is EasyDiffusionTimeoutFailure)
            assertEquals(120000L, testScheduler.currentTime)
            coVerify(exactly = 1) { fixture.repository.stop(fixture.task) }
            coVerify(exactly = 1) { fixture.registry.finish("session") }
        }

    @Test
    fun `caller cancellation keeps cancellation semantics and stops the accepted task`() = runTest {
        val entered = CompletableDeferred<Unit>()
        coEvery { fixture.repository.readStream(any(), any()) } coAnswers {
            entered.complete(Unit)
            awaitCancellation()
        }
        val generation = async { fixture.useCase(fixture.request) }
        entered.await()
        generation.cancel()
        assertTrue(runCatching { generation.await() }.exceptionOrNull() is CancellationException)
        coVerify(exactly = 1) { fixture.repository.stop(fixture.task) }
        coVerify(exactly = 1) { fixture.registry.finish("session") }
    }

    @Test
    fun `connection replacement rejects a late terminal and cleanup keeps old endpoint`() =
        runTest {
            coEvery { fixture.repository.readStream(any(), any()) } coAnswers {
                fixture.url = "https://replacement.example"
                fixture.revision++
                secondArg<suspend (EasyDiffusionFrame) -> Unit>()(fixture.terminal)
            }
            assertTrue(runCatching { fixture.useCase(fixture.request) }.isFailure)
            coVerify(exactly = 0) { fixture.repository.save(any(), any()) }
            coVerify(exactly = 1) { fixture.repository.stop(fixture.task) }
        }

    @Test
    fun `storage failures remain failures and abort remaining batch`() = runTest {
        val failure = IllegalStateException("storage unavailable")
        coEvery { fixture.repository.save(any(), any()) } throws failure
        assertRetainsFailure(
            failure,
            runCatching { fixture.useCase(fixture.request.copy(batchCount = 3)) }.exceptionOrNull()
        )
        coVerify(exactly = 1) { fixture.repository.submit(any()) }
        coVerify(exactly = 1) { fixture.repository.stop(fixture.task) }
    }

    @Test
    fun `late submission learned after caller cancellation is stopped without streaming`() =
        runTest {
            val entered = CompletableDeferred<Unit>()
            val accepted = CompletableDeferred<EasyDiffusionTask>()
            coEvery { fixture.repository.submit(any()) } coAnswers {
                entered.complete(Unit)
                accepted.await()
            }
            val generation = async { fixture.useCase(fixture.request) }
            entered.await()
            generation.cancel()
            accepted.complete(fixture.task)
            assertTrue(runCatching { generation.await() }.exceptionOrNull() is CancellationException)
            coVerify(exactly = 1) { fixture.repository.stop(fixture.task) }
            coVerify(exactly = 0) { fixture.repository.readStream(any(), any()) }
        }

    @Test
    fun `provider observation interrupts pending stream and prevents late storage`() = runTest {
        val entered = CompletableDeferred<Unit>()
        coEvery { fixture.repository.readStream(any(), any()) } coAnswers {
            entered.complete(Unit)
            awaitCancellation()
        }
        val generation = async { runCatching { fixture.useCase(fixture.request) } }
        entered.await()
        fixture.source = ServerSource.AUTOMATIC1111
        fixture.settings.value = fixture.settings.value.copy(source = fixture.source)
        assertTrue(generation.await().isFailure)
        coVerify(exactly = 1) { fixture.repository.stop(fixture.task) }
        coVerify(exactly = 0) { fixture.repository.save(any(), any()) }
    }

    @Test
    fun `invalid controls or stale selection never claim or submit work`() = runTest {
        listOf(
            fixture.request.copy(width = 513), fixture.request.copy(height = 0),
            fixture.request.copy(seed = -1), fixture.request.copy(seed = 4294967296L),
            fixture.request.copy(cfgScale = Float.NaN), fixture.request.copy(strength = 1.1f),
            fixture.request.copy(steps = 0), fixture.request.copy(batchCount = 0),
            fixture.request.copy(model = ""), fixture.request.copy(selectionRevision = 6)
        ).forEach { request ->
            assertTrue(runCatching { fixture.useCase(request) }.isFailure)
        }
        coVerify(exactly = 0) { fixture.registry.begin(any(), any(), any()) }
        coVerify(exactly = 0) { fixture.repository.submit(any()) }
    }
}
