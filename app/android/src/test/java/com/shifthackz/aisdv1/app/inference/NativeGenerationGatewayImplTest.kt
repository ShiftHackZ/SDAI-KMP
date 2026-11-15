package com.shifthackz.aisdv1.app.inference

import android.app.Application
import com.shifthackz.aisdv1.app.diagnostics.GenerationAttemptJournal
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationFailure
import com.shifthackz.aisdv1.domain.entity.NativeGenerationOwner
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.gateway.ProcessExitDiagnosticsGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises host ownership and cleanup with controlled connection/storage failures.
 * Binder transport and actual native execution are outside these unit tests.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class, sdk = [28])
class NativeGenerationGatewayImplTest {
    private val connection = mockk<InferenceConnection>(relaxed = true)
    private val connections = mockk<InferenceConnectionFactory>()
    private val files = mockk<GenerationFileStore>(relaxed = true)
    private val journal = mockk<GenerationAttemptJournal>(relaxed = true)
    private val exits = mockk<ProcessExitDiagnosticsGateway>(relaxed = true)

    @Before
    fun initialize() {
        every { connections.create() } returns connection
        every { files.readResult(any()) } returns byteArrayOf(1, 2, 3)
        coEvery { connection.close() } returns true
    }

    @Test
    fun `given completed Bonsai attempt, result is published after owned cleanup`() = runTest {
        val gateway = gateway()

        assertEquals("AQID", gateway.generate(bonsaiRequest()))

        verify(exactly = 1) { files.writeRequest(any(), bonsaiRequest()) }
        coVerify(exactly = 1) {
            connection.run(
                any(),
                NativeGenerationEngine.BONSAI,
                any(),
                any(),
                any()
            )
        }
        coVerify(exactly = 1) { connection.close() }
        verify(exactly = 1) {
            journal.finish(
                any(),
                "success",
                NativeGenerationStage.PREPARING,
                null
            )
        }
        verify(exactly = 1) { files.delete(any()) }
        assertFalse(gateway.cancelActive())
    }

    @Test
    fun `given repeated submissions, startup reconciliation runs once`() = runTest {
        val gateway = gateway()

        gateway.generate(bonsaiRequest())
        gateway.generate(bonsaiRequest())

        verify(exactly = 1) { journal.reconcile() }
        verify(exactly = 1) { files.clearAbandoned() }
        verify(exactly = 2) { connections.create() }
    }

    @Test
    fun `given child callbacks, Bonsai progress and stage stay separate from other engines`() =
        runTest {
            val gateway = gateway()
            coEvery { connection.run(any(), any(), any(), any(), any()) } coAnswers {
                arg<suspend (Int, String) -> Unit>(4)(123, "incarnation")
                arg<(LocalDiffusionStatus) -> Unit>(2)(LocalDiffusionStatus(2, 4))
                arg<(NativeGenerationStage) -> Unit>(3)(NativeGenerationStage.DENOISING)
                assertEquals(
                    LocalDiffusionStatus(2, 4),
                    gateway.observeStatus(NativeGenerationEngine.BONSAI).first()
                )
                assertEquals(
                    NativeGenerationStage.DENOISING,
                    gateway.observeStage(NativeGenerationEngine.BONSAI).first()
                )
                assertEquals(
                    LocalDiffusionStatus(0, 0),
                    gateway.observeStatus(NativeGenerationEngine.ONNX).first()
                )
            }

            gateway.generate(bonsaiRequest())

            verify { journal.connected(any(), 123, "incarnation") }
            verify { journal.finish(any(), "success", NativeGenerationStage.DENOISING, null) }
            assertEquals(
                LocalDiffusionStatus(0, 0),
                gateway.observeStatus(NativeGenerationEngine.BONSAI).first()
            )
        }

    @Test
    fun `given concurrent request, busy failure preserves current worker and requested backend`() =
        runTest {
            val gateway = gateway()
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { connection.run(any(), any(), any(), any(), any()) } coAnswers {
                entered.complete(Unit)
                release.await()
            }
            val first = async { gateway.generate(bonsaiRequest()) }
            entered.await()

            val failure =
                runCatching { gateway.generate(bonsaiRequest()) }.exceptionOrNull() as NativeGenerationFailure

            assertEquals(NativeGenerationFailure.Category.BUSY, failure.category)
            assertEquals(NativeGenerationEngine.BONSAI, failure.engine)
            assertEquals(BonsaiBackend.CPU, failure.bonsaiBackend)
            coVerify(exactly = 0) { connection.cancel() }
            verify(exactly = 1) { connections.create() }
            release.complete(Unit)
            assertEquals("AQID", first.await())
        }

    @Test
    fun `given stale worker cancellation, only exact current owner can cancel`() = runTest {
        val gateway = gateway()
        val entered = CompletableDeferred<Unit>()
        coEvery { connection.run(any(), any(), any(), any(), any()) } coAnswers {
            entered.complete(Unit)
            awaitCancellation()
        }
        val attempt =
            async(NativeGenerationOwner("current-worker")) { gateway.generate(bonsaiRequest()) }
        entered.await()

        assertFalse(gateway.cancelActive("old-worker"))
        coVerify(exactly = 0) { connection.cancel() }
        assertTrue(gateway.cancelActive("current-worker"))
        coVerify(exactly = 1) { connection.cancel() }
        attempt.cancelAndJoin()
        coVerify(exactly = 1) { connection.close() }
        verify { journal.finish(any(), "cancelled", NativeGenerationStage.PREPARING, null) }
        verify { files.delete(any()) }
    }

    @Test
    fun `given sequential worker batch, each image has a distinct stable owner key`() = runTest {
        val gateway = gateway()
        val attempt = async(NativeGenerationOwner("batch-worker")) {
            gateway.generate(bonsaiRequest())
            gateway.generate(bonsaiRequest())
        }
        attempt.await()

        verify(exactly = 1) {
            journal.begin(
                any(),
                NativeGenerationEngine.BONSAI,
                "batch-worker:0"
            )
        }
        verify(exactly = 1) {
            journal.begin(
                any(),
                NativeGenerationEngine.BONSAI,
                "batch-worker:1"
            )
        }
    }

    @Test
    fun `given child death, failure keeps Bonsai context and never retries automatically`() =
        runTest {
            coEvery { connection.run(any(), any(), any(), any(), any()) } throws
                    NativeGenerationFailure(NativeGenerationFailure.Category.PROCESS_DIED)
            val gateway = gateway()

            val failure =
                runCatching { gateway.generate(bonsaiRequest()) }.exceptionOrNull() as NativeGenerationFailure

            assertEquals(NativeGenerationFailure.Category.PROCESS_DIED, failure.category)
            assertEquals(NativeGenerationEngine.BONSAI, failure.engine)
            assertEquals(BonsaiBackend.CPU, failure.bonsaiBackend)
            assertNull(failure.backend)
            verify(exactly = 1) { connections.create() }
            coVerify(exactly = 1) { connection.run(any(), any(), any(), any(), any()) }
            coVerify(exactly = 1) { exits.readInferenceExits(any(), any()) }
            verify { journal.finish(any(), "process_died", NativeGenerationStage.PREPARING, any()) }
            verify { files.delete(any()) }
        }

    @Test
    fun `given invalid output, host returns controlled failure after cleanup`() = runTest {
        every { files.readResult(any()) } throws IllegalArgumentException("invalid output")

        val failure =
            runCatching { gateway().generate(bonsaiRequest()) }.exceptionOrNull() as NativeGenerationFailure

        assertEquals(NativeGenerationFailure.Category.INVALID_RESULT, failure.category)
        assertEquals(BonsaiBackend.CPU, failure.bonsaiBackend)
        coVerify(exactly = 1) { connection.close() }
        verify { files.delete(any()) }
    }

    @Test
    fun `given child cleanup failure, later admission is quarantined and files are retained`() =
        runTest {
            coEvery { connection.close() } returns false
            val firstJob = slot<String>()
            every { files.writeRequest(capture(firstJob), any()) } returns Unit
            val gateway = gateway()
            gateway.generate(bonsaiRequest())
            val retainedJob = firstJob.captured

            val failure =
                runCatching { gateway.generate(bonsaiRequest()) }.exceptionOrNull() as NativeGenerationFailure

            assertEquals(NativeGenerationFailure.Category.UNRESPONSIVE, failure.category)
            verify(exactly = 1) { connections.create() }
            verify(exactly = 0) { files.delete(retainedJob) }
        }

    @Test
    fun `given journal rejects replayed owner, no child is started`() = runTest {
        every { journal.begin(any(), any(), any()) } throws
                NativeGenerationFailure(NativeGenerationFailure.Category.INTERRUPTED)

        val failure =
            runCatching { gateway().generate(bonsaiRequest()) }.exceptionOrNull() as NativeGenerationFailure

        assertEquals(NativeGenerationFailure.Category.INTERRUPTED, failure.category)
        verify(exactly = 0) { connections.create() }
        verify(exactly = 0) { files.writeRequest(any(), any()) }
        verify(exactly = 0) { journal.finish(any(), any(), any(), any()) }
    }

    @Test
    fun `given terminal journal write fails, image is not published and later work is quarantined`() =
        runTest {
            every {
                journal.finish(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws IllegalStateException("disk unavailable")
            val gateway = gateway()

            val first =
                runCatching { gateway.generate(bonsaiRequest()) }.exceptionOrNull() as NativeGenerationFailure
            val second =
                runCatching { gateway.generate(bonsaiRequest()) }.exceptionOrNull() as NativeGenerationFailure

            assertEquals(NativeGenerationFailure.Category.INVALID_RESULT, first.category)
            assertEquals(NativeGenerationFailure.Category.UNRESPONSIVE, second.category)
            verify(exactly = 1) { connections.create() }
        }

    private fun TestScope.gateway() =
        NativeGenerationGatewayImpl(connections, files, journal, exits, backgroundScope)
}
