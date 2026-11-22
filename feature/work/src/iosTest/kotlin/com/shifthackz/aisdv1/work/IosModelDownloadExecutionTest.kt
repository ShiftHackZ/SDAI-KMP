package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.work.fixture.ControlledModelDownloads
import com.shifthackz.aisdv1.work.fixture.RecordingIosWorkExecution
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Checks native admission and per-owner cancellation around the iOS model-download manager.
 * These component scenarios do not replace real application download and installation QA.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("INVALID_CHARACTERS_NATIVE_ERROR")
class IosModelDownloadExecutionTest {
    /**
     * Given a selected model, admission is delayed, expected transfer collection only after readiness.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given a selected model, admission is delayed, expected transfer collection only after readiness`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val request = singleRequest("first")

            // When
            manager.start(request, false)
            runCurrent()
            val ownerId = execution.readiness.keys.single()

            // Then
            assertTrue(downloads.started.isEmpty())
            assertEquals(false, execution.gpuRequirements[ownerId])
            execution.readiness.getValue(ownerId)()
            runCurrent()
            assertEquals(listOf(request), downloads.started)
        }

    /**
     * Given a two-model batch, the second model is halfway downloaded, expected aggregate progress until final installation.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given a two-model batch, the second model is halfway downloaded, expected aggregate progress until final installation`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val request = ModelDownloadRequest(LocalAiModel.Type.CoreMl)
            manager.start(request, false)
            runCurrent()
            val ownerId = execution.readiness.keys.single()
            execution.readiness.getValue(ownerId)()
            runCurrent()

            // When
            val transferring = ModelDownloadBatch(
                request.type, modelId = "second", totalModelCount = 2,
                completedIds = listOf("first"), progress = DownloadState.Downloading(50),
            )
            downloads.streams.getValue(request).send(transferring)
            runCurrent()

            // Then
            assertEquals(75, execution.progress[ownerId])
            assertTrue(execution.outcomes.isEmpty())
            downloads.streams.getValue(request).send(
                transferring.copy(
                    completedIds = listOf("first", "second"),
                    progress = DownloadState.Complete("installed"),
                    isRunning = false
                )
            )
            downloads.streams.getValue(request).close()
            runCurrent()
            assertEquals(true, execution.outcomes[ownerId])
            assertFalse(manager.batches.value.single().isRunning)
        }

    /**
     * Given concurrent single downloads, one native task expires, expected only its transfer cancelled.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given concurrent single downloads, one native task expires, expected only its transfer cancelled`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val first = singleRequest("first")
            val second = singleRequest("second")
            manager.start(first, false)
            manager.start(second, false)
            runCurrent()
            val ownerIds = execution.readiness.keys.toList()
            execution.readiness.values.forEach { it() }
            runCurrent()

            // When
            execution.expirations.getValue(ownerIds.first())()
            runCurrent()
            downloads.streams.getValue(second).send(
                ModelDownloadBatch(
                    second.type,
                    modelId = second.modelId,
                    isBatch = false,
                    totalModelCount = 1,
                    progress = DownloadState.Downloading(35)
                )
            )
            runCurrent()

            // Then
            assertEquals(false, execution.outcomes[ownerIds.first()])
            assertFalse(execution.outcomes.containsKey(ownerIds.last()))
            assertEquals(35, execution.progress[ownerIds.last()])
            assertFalse(manager.batches.value.first { it.modelId == first.modelId }.isRunning)
            assertTrue(manager.batches.value.first { it.modelId == second.modelId }.isRunning)
        }

    /**
     * Given pending native admission, the selected model is cancelled, expected late readiness cannot start a transfer.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given pending native admission, the selected model is cancelled, expected late readiness cannot start a transfer`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val request = singleRequest("first")
            manager.start(request, false)
            runCurrent()
            val ownerId = execution.readiness.keys.single()

            // When
            manager.cancel(request.type, request.modelId)
            runCurrent()
            execution.readiness.getValue(ownerId)()
            runCurrent()

            // Then
            assertTrue(downloads.started.isEmpty())
            assertEquals(false, execution.outcomes[ownerId])
            assertFalse(manager.batches.value.single().isRunning)
        }

    /**
     * Given an active model transfer, the download fails, expected the original failure and unsuccessful native completion.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given an active model transfer, the download fails, expected the original failure and unsuccessful native completion`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val request = singleRequest("first")
            val failure = IllegalStateException("Storage is full")
            manager.start(request, false)
            runCurrent()
            val ownerId = execution.readiness.keys.single()
            execution.readiness.getValue(ownerId)()
            runCurrent()

            // When
            downloads.streams.getValue(request).close(failure)
            runCurrent()

            // Then
            assertSame(
                failure,
                assertIs<DownloadState.Error>(manager.batches.value.single().progress).error
            )
            assertEquals(false, execution.outcomes[ownerId])
        }

    /**
     * Given a replacement model transfer, an old expiration callback arrives, expected replacement work remains active.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given a replacement model transfer, an old expiration callback arrives, expected replacement work remains active`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            manager.start(singleRequest("first"), false)
            runCurrent()
            val previousId = execution.readiness.keys.single()
            execution.readiness.getValue(previousId)()
            runCurrent()
            manager.start(singleRequest("replacement"), true)
            runCurrent()
            val replacementId = execution.readiness.keys.last()
            execution.readiness.getValue(replacementId)()
            runCurrent()

            // When
            execution.expirations.getValue(previousId)()
            runCurrent()

            // Then
            assertTrue(manager.batches.value.first { it.workId == replacementId }.isRunning)
            assertFalse(manager.batches.value.first { it.workId == previousId }.isRunning)
            assertFalse(execution.outcomes.containsKey(replacementId))
        }


    @Test
    fun `given a model download, native admission is rejected, expected an error without starting a transfer`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            manager.start(singleRequest("first"), false)
            runCurrent()
            val owner = execution.readiness.keys.single()
            // When
            execution.rejections.getValue(owner)()
            runCurrent()
            // Then
            assertTrue(downloads.started.isEmpty())
            assertIs<DownloadState.Error>(manager.batches.value.single().progress)
            assertFalse(manager.batches.value.single().isRunning)
            assertEquals(false, execution.outcomes[owner])
        }

    @Test
    fun `given an admitted download, the stream ends without an installation outcome, expected failure instead of false success`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val request = singleRequest("first")
            manager.start(request, false)
            runCurrent()
            val owner = execution.readiness.keys.single()
            execution.readiness.getValue(owner)()
            runCurrent()
            // When
            downloads.streams.getValue(request).close()
            runCurrent()
            // Then
            assertEquals(false, execution.outcomes[owner])
            assertIs<DownloadState.Error>(manager.batches.value.single().progress)
        }

    @Test
    fun `given an admitted download, the stream terminates with an error snapshot, expected failure without a successful native outcome`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val request = singleRequest("first")
            manager.start(request, false)
            runCurrent()
            val owner = execution.readiness.keys.single()
            execution.readiness.getValue(owner)()
            runCurrent()
            // When
            downloads.streams.getValue(request).send(
                ModelDownloadBatch(
                    request.type,
                    progress = DownloadState.Error(IllegalStateException("activation failed")),
                    isRunning = false,
                )
            )
            downloads.streams.getValue(request).close()
            runCurrent()
            // Then
            assertEquals(false, execution.outcomes[owner])
            assertFalse(manager.batches.value.single().isRunning)
        }

    @Test
    fun `given a download with no known model count, a loading snapshot arrives, expected indeterminate native progress`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val request = singleRequest("first")
            manager.start(request, false)
            runCurrent()
            val owner = execution.readiness.keys.single()
            execution.readiness.getValue(owner)()
            runCurrent()
            // When
            downloads.streams.getValue(request).send(ModelDownloadBatch(request.type))
            runCurrent()
            // Then
            assertEquals(null, execution.progress[owner])
            assertTrue(execution.outcomes.isEmpty())
        }

    @Test
    fun `given a batch with installed models, system expiration cancels the current transfer, expected completed model identities remain retained`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val request = ModelDownloadRequest(LocalAiModel.Type.CoreMl)
            manager.start(request, false)
            runCurrent()
            val owner = execution.readiness.keys.single()
            execution.readiness.getValue(owner)()
            runCurrent()
            downloads.streams.getValue(request).send(
                ModelDownloadBatch(
                    request.type, completedIds = listOf("installed"), totalModelCount = 2,
                    progress = DownloadState.Downloading(30),
                )
            )
            runCurrent()
            // When
            execution.expirations.getValue(owner)()
            runCurrent()
            // Then
            assertEquals(listOf("installed"), manager.batches.value.single().completedIds)
            assertFalse(manager.batches.value.single().isRunning)
            assertEquals(false, execution.outcomes[owner])
        }

    @Test
    fun `given an active single download, the same model is requested again, expected one native execution session`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            val request = singleRequest("first")
            manager.start(request, false)
            runCurrent()
            // When
            manager.start(request, false)
            runCurrent()
            // Then
            assertEquals(1, execution.readiness.size)
            assertEquals(1, manager.batches.value.count { it.isRunning })
        }

    @Test
    fun `given an active single download, Download all is requested without replacement confirmation, expected the existing transfer is retained`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            manager.start(singleRequest("first"), false)
            runCurrent()
            // When
            manager.start(ModelDownloadRequest(LocalAiModel.Type.CoreMl), false)
            runCurrent()
            // Then
            assertEquals(1, execution.readiness.size)
            assertFalse(manager.batches.value.single().isBatch)
            assertTrue(manager.batches.value.single().isRunning)
        }

    @Test
    fun `given an active batch, another single download is requested, expected the exclusive batch is retained`() =
        runTest {
            // Given
            val downloads = ControlledModelDownloads()
            val execution = RecordingIosWorkExecution()
            val manager = ModelDownloadManagerImpl(downloads, execution, backgroundScope)
            manager.start(ModelDownloadRequest(LocalAiModel.Type.CoreMl), false)
            runCurrent()
            // When
            manager.start(singleRequest("first"), false)
            runCurrent()
            // Then
            assertEquals(1, execution.readiness.size)
            assertTrue(manager.batches.value.single().isBatch)
        }

    /**
     * Captures a single-model mirror without invoking any network or archive implementation.
     *
     * @param modelId Catalog identity distinguishing concurrent fixture streams.
     * @author Dmitriy Moroz
     */
    private fun singleRequest(modelId: String) = ModelDownloadRequest(
        LocalAiModel.Type.CoreMl, modelId, "https://models.example/$modelId.zip"
    )
}
