package com.shifthackz.aisdv1.work.task

import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelsUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Exercises production worker inputs, progress and terminal decisions with notification delivery isolated.
 * Android notification appearance is verified separately through the real application.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class DownloadModelsTaskTest {
    private val download = mockk<DownloadModelsUseCase>()
    private val type = LocalAiModel.Type.ONNX

    @Test
    fun `given single model worker, selected archive activates, expected captured mirror and completion notification`() =
        runTest {
            // Given
            val request = ModelDownloadRequest(type, "model", "https://models/selected")
            every { download(request) } returns flowOf(
                ModelDownloadBatch(
                    type,
                    "model",
                    DownloadState.Complete("/owned/model"),
                    listOf("model"),
                    false,
                    "Model",
                    1,
                    false
                )
            )
            val worker = worker(request)
            // When
            val result = worker.doWork()
            // Then
            assertTrue(result is Result.Success)
            verify(exactly = 1) { download(request) }
            verify(exactly = 1) {
                worker.showGenericNotification(
                    "Download complete 🤩",
                    "Click here to open the application."
                )
            }
            assertEquals("/owned/model", (result as Result.Success).outputData.getString("path"))
            assertEquals(listOf("model"), result.outputData.getStringArray("completed")?.toList())
        }

    @Test
    fun `given provider batch worker, second model is half downloaded, expected total progress and cancel all label`() =
        runTest {
            // Given
            val request = ModelDownloadRequest(type)
            every { download(request) } returns flowOf(
                ModelDownloadBatch(
                    type,
                    "second",
                    DownloadState.Downloading(50),
                    listOf("first"),
                    modelName = "Second",
                    totalModelCount = 4
                ),
                ModelDownloadBatch(
                    type,
                    isRunning = false,
                    totalModelCount = 4,
                    completedIds = listOf("first", "second", "third", "fourth")
                ),
            )
            val worker = worker(request)
            // When
            worker.doWork()
            // Then
            verify {
                worker.setForegroundNotification(
                    "Download all",
                    "Second",
                    null,
                    37 to 100,
                    true,
                    true,
                    "Cancel all downloads"
                )
            }
            verify(exactly = 1) { worker.showGenericNotification("Download complete 🤩", any()) }
        }

    @Test
    fun `given single model worker, download fails, expected failure output and failed notification without success`() =
        runTest {
            // Given
            val request = ModelDownloadRequest(type, "model", "https://models/model")
            every { download(request) } returns flow { throw IllegalStateException("offline") }
            val worker = worker(request)
            // When
            val result = worker.doWork()
            // Then
            assertTrue(result is Result.Failure)
            assertEquals("offline", (result as Result.Failure).outputData.getString("error"))
            verify(exactly = 1) {
                worker.showGenericNotification(
                    "Download failed 😞",
                    "Click here to open the application."
                )
            }
            verify(exactly = 0) { worker.showGenericNotification("Download complete 🤩", any()) }
        }

    @Test
    fun `given provider worker, cancellation propagates, expected no terminal notification`() =
        runTest {
            // Given
            val request = ModelDownloadRequest(type)
            val cancellation = CancellationException("cancelled")
            every { download(request) } returns flow { throw cancellation }
            val worker = worker(request)
            // When
            val actual = runCatching { worker.doWork() }.exceptionOrNull()
            // Then
            assertSame(cancellation, actual)
            verify(exactly = 0) { worker.showGenericNotification(any(), any()) }
        }

    @Test
    fun `given concurrent model workers, requests have distinct identities, expected independent foreground and result notifications`() {
        // Given
        val request = ModelDownloadRequest(type)
        // When
        val first = worker(request)
        val second = worker(request)
        // Then
        assertNotEquals(first.notificationId, second.notificationId)
        assertNotEquals(first.genericNotificationId, second.genericNotificationId)
        assertNotEquals(first.notificationId, second.genericNotificationId)
        assertNotEquals(first.notificationId, first.genericNotificationId)
    }

    @Test
    fun `given download worker, stop races with activation completion, expected no terminal notification`() =
        runTest {
            // Given
            val request = ModelDownloadRequest(type)
            every { download(request) } returns flowOf(ModelDownloadBatch(type, isRunning = false))
            val worker = worker(request)
            every { worker.isStopped } returns true
            // When
            val actual = runCatching { worker.doWork() }.exceptionOrNull()
            // Then
            assertTrue(actual is CancellationException)
            verify(exactly = 0) { worker.showGenericNotification(any(), any()) }
        }

    @Test
    fun `given failed download worker, exception has no message, expected bounded diagnostic text in failure output`() =
        runTest {
            // Given
            val request = ModelDownloadRequest(type)
            val failure = IllegalStateException()
            every { download(request) } returns flow { throw failure }
            val worker = worker(request)
            // When
            val result = worker.doWork()
            // Then
            assertEquals(
                failure.toString(),
                (result as Result.Failure).outputData.getString("error")
            )
            verify(exactly = 1) { worker.showGenericNotification("Download failed 😞", any()) }
        }

    /**
     * Supplies normal WorkManager request data while preventing platform service/notification side effects.
     *
     * @param request Captured provider and optional model/mirror to place in worker input.
     * @author Dmitriy Moroz
     */
    private fun worker(request: ModelDownloadRequest): DownloadModelsTask {
        val parameters = mockk<WorkerParameters>(relaxed = true) {
            every { id } returns UUID.randomUUID()
            every { inputData } returns workDataOf(
                "type" to request.type.key,
                "model" to request.modelId,
                "url" to request.sourceUrl
            )
        }
        return spyk(
            DownloadModelsTask(
                RuntimeEnvironment.getApplication(),
                parameters,
                mockk(),
                mockk(),
                download
            )
        ).also { worker ->
            coEvery { worker.setProgress(any()) } returns Unit
            every {
                worker.setForegroundNotification(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } just Runs
            every { worker.showGenericNotification(any(), any()) } just Runs
        }
    }
}
