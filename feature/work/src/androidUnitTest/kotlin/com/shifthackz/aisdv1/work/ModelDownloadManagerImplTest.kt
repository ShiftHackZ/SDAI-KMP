package com.shifthackz.aisdv1.work

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.work.di.WorkManagerProvider
import com.shifthackz.aisdv1.work.task.DownloadModelsTask
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies durable admission, targeted cancellation and progress reconstruction at the WorkManager boundary.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ModelDownloadManagerImplTest {
    private val workManager = mockk<WorkManager>()
    private val infos = MutableStateFlow<List<WorkInfo>>(emptyList())
    private val operation = mockk<Operation>(relaxed = true) {
        every { result.get() } returns Operation.SUCCESS
    }

    @Test
    fun `given single model admission, another single is active, expected unique concurrent work with confirmed input`() =
        runTest {
            // Given
            infos.value = listOf(info(WorkInfo.State.RUNNING, "first"))
            val request =
                ModelDownloadRequest(LocalAiModel.Type.ONNX, "second", "https://models/second")
            val queued = slot<OneTimeWorkRequest>()
            every { workManager.enqueueUniqueWork(any(), any(), capture(queued)) } returns operation
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            manager.start(request)
            // Then
            verify {
                workManager.enqueueUniqueWork(
                    "download_all_models:onnx:second",
                    ExistingWorkPolicy.KEEP,
                    any<OneTimeWorkRequest>()
                )
            }
            assertEquals("second", queued.captured.workSpec.input.getString("model"))
            assertEquals(request.sourceUrl, queued.captured.workSpec.input.getString("url"))
            assertTrue("mode:single" in queued.captured.tags)
            verify(exactly = 0) { workManager.cancelAllWorkByTag(any()) }
        }

    @Test
    fun `given provider batch admission, a single model is active, expected existing work retained until confirmed switch`() =
        runTest {
            // Given
            infos.value = listOf(info(WorkInfo.State.RUNNING, "first"))
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            manager.start(ModelDownloadRequest(LocalAiModel.Type.MediaPipe))
            // Then
            verify(exactly = 0) {
                workManager.enqueueUniqueWork(
                    any(),
                    any(),
                    any<OneTimeWorkRequest>()
                )
            }
            verify(exactly = 0) { workManager.cancelAllWorkByTag(any()) }
        }

    @Test
    fun `given exclusive provider batch, a single model is requested, expected no additional worker`() =
        runTest {
            // Given
            infos.value = listOf(info(WorkInfo.State.RUNNING))
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            manager.start(
                ModelDownloadRequest(
                    LocalAiModel.Type.ONNX,
                    "first",
                    "https://models/first"
                )
            )
            // Then
            verify(exactly = 0) {
                workManager.enqueueUniqueWork(
                    any(),
                    any(),
                    any<OneTimeWorkRequest>()
                )
            }
        }

    @Test
    fun `given provider switch, replacement is confirmed, expected old work cancelled before new batch enqueued`() =
        runTest {
            // Given
            infos.value = listOf(info(WorkInfo.State.RUNNING, "first"))
            every { workManager.cancelAllWorkByTag(any()) } returns operation
            every {
                workManager.enqueueUniqueWork(
                    any(),
                    any(),
                    any<OneTimeWorkRequest>()
                )
            } returns operation
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            manager.start(ModelDownloadRequest(LocalAiModel.Type.MediaPipe), shouldReplace = true)
            // Then
            verifyOrder {
                workManager.cancelAllWorkByTag(DownloadModelsTask.WORK_NAME)
                workManager.enqueueUniqueWork(
                    "download_all_models:mediapipe:all",
                    ExistingWorkPolicy.REPLACE,
                    any<OneTimeWorkRequest>()
                )
            }
        }

    @Test
    fun `given single model cancellation, other transfers exist, expected only selected unique request cancelled`() =
        runTest {
            // Given
            every { workManager.cancelUniqueWork(any()) } returns operation
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            manager.cancel(LocalAiModel.Type.ONNX, "first")
            // Then
            verify(exactly = 1) { workManager.cancelUniqueWork("download_all_models:onnx:first") }
            verify(exactly = 0) { workManager.cancelAllWorkByTag(any()) }
        }

    @Test
    fun `given all model cancellation, no provider is selected, expected every model worker cancelled by shared tag`() =
        runTest {
            // Given
            every { workManager.cancelAllWorkByTag(any()) } returns operation
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            manager.cancel()
            // Then
            verify(exactly = 1) { workManager.cancelAllWorkByTag(DownloadModelsTask.WORK_NAME) }
        }

    @Test
    fun `given persisted work, active request follows newer terminal timestamp, expected running progress restored`() =
        runTest {
            // Given
            val running = info(WorkInfo.State.RUNNING, "first", timestamp = 1)
            val finished = info(WorkInfo.State.SUCCEEDED, "first", timestamp = 2)
            infos.value = listOf(finished, running)
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            val snapshot = manager.batches.first().single()
            // Then
            assertTrue(snapshot.isRunning)
            assertEquals(running.id.toString(), snapshot.workId)
            assertEquals(DownloadState.Downloading(42), snapshot.progress)
            assertFalse(snapshot.isBatch)
        }

    @Test
    fun `given cancelled persisted work, output has no progress, expected cancelled state restored without error`() =
        runTest {
            // Given
            infos.value = listOf(info(WorkInfo.State.CANCELLED, "first"))
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            val snapshot = manager.batches.first().single()
            // Then
            assertFalse(snapshot.isRunning)
            assertEquals(DownloadState.Unknown, snapshot.progress)
            assertEquals("first", snapshot.modelId)
        }

    @Test
    fun `given failed persisted work, error output exists, expected failure restored without successful fallback`() =
        runTest {
            // Given
            infos.value = listOf(info(WorkInfo.State.FAILED, "first"))
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            val snapshot = manager.batches.first().single()
            // Then
            assertFalse(snapshot.isRunning)
            assertEquals("offline", (snapshot.progress as DownloadState.Error).error.message)
        }

    @Test
    fun `given finished worker history, newer archive activation follows an old failure, expected latest successful ownership restored`() =
        runTest {
            // Given
            val old = info(WorkInfo.State.FAILED, "first", timestamp = 1)
            val latest = info(WorkInfo.State.SUCCEEDED, "first", timestamp = 2)
            every { latest.outputData } returns workDataOf(
                "type" to "onnx",
                "model" to "first",
                "path" to "/owned/first",
                "completed" to arrayOf("first"),
                "total_count" to 1
            )
            infos.value = listOf(old, latest)
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            val result = manager.batches.first().single()
            // Then
            assertEquals(latest.id.toString(), result.workId)
            assertEquals(DownloadState.Complete("/owned/first"), result.progress)
            assertEquals(listOf("first"), result.completedIds)
            assertFalse(result.isRunning)
        }

    @Test
    fun `given failed worker without error payload, scheduler reports failure, expected explicit failure rather than success`() =
        runTest {
            // Given
            val failed = info(WorkInfo.State.FAILED, "first")
            every { failed.outputData } returns workDataOf()
            infos.value = listOf(failed)
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            val result = manager.batches.first().single()
            // Then
            assertEquals(
                "Model download failed",
                (result.progress as DownloadState.Error).error.message
            )
        }

    @Test
    fun `given newly queued provider batch, no active model is known, expected running batch with pending progress`() =
        runTest {
            // Given
            infos.value = listOf(info(WorkInfo.State.ENQUEUED))
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            val result = manager.batches.first().single()
            // Then
            assertTrue(result.isRunning && result.isBatch)
            assertEquals(null, result.modelId)
            assertEquals(null, result.progress)
        }

    @Test
    fun `given persisted record without provider metadata, work is observed, expected malformed record omitted`() =
        runTest {
            // Given
            val malformed = info(WorkInfo.State.ENQUEUED)
            every { malformed.tags } returns setOf(DownloadModelsTask.WORK_NAME)
            infos.value = listOf(malformed)
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            val result = manager.batches.first()
            // Then
            assertTrue(result.isEmpty())
        }

    @Test
    fun `given idle provider batch request, no workers exist, expected unique batch enqueued with keep policy`() =
        runTest {
            // Given
            every {
                workManager.enqueueUniqueWork(
                    any(),
                    any(),
                    any<OneTimeWorkRequest>()
                )
            } returns operation
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            manager.start(ModelDownloadRequest(LocalAiModel.Type.ONNX))
            // Then
            verify(exactly = 1) {
                workManager.enqueueUniqueWork(
                    "download_all_models:onnx:all",
                    ExistingWorkPolicy.KEEP,
                    any<OneTimeWorkRequest>()
                )
            }
        }

    @Test
    fun `given provider cancellation, other providers may be active, expected only requested provider tag cancelled`() =
        runTest {
            // Given
            every { workManager.cancelAllWorkByTag(any()) } returns operation
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            manager.cancel(LocalAiModel.Type.ONNX)
            // Then
            verify(exactly = 1) { workManager.cancelAllWorkByTag("provider:onnx") }
            verify(exactly = 0) { workManager.cancelUniqueWork(any()) }
        }

    @Test
    fun `given single model cancellation, owning provider is absent, expected ambiguous cancellation rejected`() =
        runTest {
            // Given
            val manager = manager(StandardTestDispatcher(testScheduler))
            // When
            val failure = runCatching { manager.cancel(null, "first") }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            verify(exactly = 0) { workManager.cancelAllWorkByTag(any()) }
            verify(exactly = 0) { workManager.cancelUniqueWork(any()) }
        }

    /**
     * Binds scheduling to virtual time while retaining production request construction and admission policy.
     * @param dispatcher Scheduler shared with the owning coroutine test.
     * @author Dmitriy Moroz
     */
    private fun manager(dispatcher: CoroutineDispatcher): ModelDownloadManagerImpl {
        every { workManager.getWorkInfosByTagFlow(DownloadModelsTask.WORK_NAME) } returns infos
        val dispatchers = mockk<DispatchersProvider> { every { io } returns dispatcher }
        return ModelDownloadManagerImpl(WorkManagerProvider { workManager }, dispatchers)
    }

    /**
     * Supplies a persisted WorkInfo record with real worker tags and independent progress/output data.
     * @param state Persisted scheduler lifecycle.
     * @param model Single-model identity, or null for an exclusive provider batch.
     * @param timestamp Enqueue ordering used to choose terminal history.
     * @author Dmitriy Moroz
     */
    private fun info(state: WorkInfo.State, model: String? = null, timestamp: Long = 1): WorkInfo =
        mockk {
            every { id } returns UUID.randomUUID()
            every { this@mockk.state } returns state
            every { tags } returns buildSet {
                add("provider:onnx"); add("download:onnx:${model ?: "all"}"); add("enqueued:$timestamp")
                if (model != null) {
                    add("mode:single"); add("model:$model")
                }
            }
            every { progress } returns workDataOf("percent" to 42, "total_count" to 1)
            every { outputData } returns workDataOf("error" to "offline")
        }
}
