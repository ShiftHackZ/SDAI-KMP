package com.shifthackz.aisdv1.work

import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.workDataOf
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.work.di.WorkManagerProvider
import com.shifthackz.aisdv1.work.mappers.toByteArray
import com.shifthackz.aisdv1.work.task.ImageToImageTask
import com.shifthackz.aisdv1.work.task.TextToImageTask
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Queues immutable work snapshots off main, preserving request identity across worker recreation.
 *
 * @param workManager WorkManager bridge supplied by composition.
 * @param payloads Atomic payload store.
 * @param observer Existing application failure/status channel.
 * @param scope Host-owned IO scheduling scope.
 * @author Dmitriy Moroz
 */
internal class BackgroundTaskManagerImpl(
    private val workManager: WorkManagerProvider,
    private val payloads: GenerationWorkPayloadStore,
    private val observer: BackgroundWorkObserver,
    private val scope: CoroutineScope,
) : BackgroundTaskManager {
    private val scheduling = Mutex()
    private val admission = Any()
    private var epoch = 0L
    private var cancellation = CompletableDeferred<Unit>().also { it.complete(Unit) }

    /**
     * Schedules serialization and enqueueing off main using a fresh per-work payload snapshot.
     * Submission failures are delivered through the background-work observer.
     *
     * @param payload Normal submitted txt2img payload.
     * @author Dmitriy Moroz
     */
    override fun scheduleTextToImageTask(payload: TextToImagePayload) {
        submit { enqueue<TextToImageTask>(payload.toByteArray(), Constants.FILE_TEXT_TO_IMAGE) }
    }

    /**
     * Schedules img2img serialization and enqueueing off main with its own work identity.
     * Submission failures are delivered through the background-work observer.
     *
     * @param payload Normal submitted img2img payload.
     * @author Dmitriy Moroz
     */
    override fun scheduleImageToImageTask(payload: ImageToImagePayload) {
        submit { enqueue<ImageToImageTask>(payload.toByteArray(), Constants.FILE_IMAGE_TO_IMAGE) }
    }

    /**
     * Explicit user retry always obtains a fresh work UUID.
     *
     * @author Dmitriy Moroz
     */
    override fun retryLastTextToImageTask(): Result<Unit> {
        submit {
            enqueue<TextToImageTask>(
                payloads.latest(Constants.FILE_TEXT_TO_IMAGE),
                Constants.FILE_TEXT_TO_IMAGE
            )
        }
        return Result.success(Unit)
    }

    /**
     * Explicit user retry always obtains a fresh work UUID.
     *
     * @author Dmitriy Moroz
     */
    override fun retryLastImageToImageTask(): Result<Unit> {
        submit {
            enqueue<ImageToImageTask>(
                payloads.latest(Constants.FILE_IMAGE_TO_IMAGE),
                Constants.FILE_IMAGE_TO_IMAGE
            )
        }
        return Result.success(Unit)
    }

    /**
     * Cancels existing WorkManager requests; each worker cancels only its owned native attempt.
     *
     * @author Dmitriy Moroz
     */
    override fun cancelAll(): Result<Unit> {
        val (previous, barrier, cancelledEpoch) = synchronized(admission) {
            epoch++
            val previous = cancellation
            val barrier = CompletableDeferred<Unit>()
            cancellation = barrier
            Triple(previous, barrier, epoch)
        }
        scope.launch {
            try {
                runCatching { previous.await() }
                scheduling.withLock {
                    workManager().cancelAllWork().result.get()
                    prunePayloads()
                }
                barrier.complete(Unit)
            } catch (t: Exception) {
                barrier.completeExceptionally(t)
                synchronized(admission) { if (cancelledEpoch == epoch) observer.postFailedSignal(t) }
            }
        }
        return Result.success(Unit)
    }

    private fun submit(action: suspend () -> Unit) {
        val (submittedEpoch, barrier) = synchronized(admission) { ++epoch to cancellation }
        scope.launch {
            runCatching {
                barrier.await()
                scheduling.withLock {
                    if (synchronized(admission) { submittedEpoch == epoch }) action()
                }
            }.onFailure { failure ->
                if (failure is CancellationException) throw failure
                synchronized(admission) {
                    if (submittedEpoch == epoch) observer.postFailedSignal(
                        failure
                    )
                }
            }
        }
    }

    private inline fun <reified W : ListenableWorker> enqueue(bytes: ByteArray, latest: String) {
        val request = OneTimeWorkRequestBuilder<W>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(workDataOf(Constants.IMMUTABLE_PAYLOAD to true))
            .addTag(Constants.TAG_GENERATION).build()
        payloads.write(request.id.toString(), latest, bytes)
        observer.claimOwner(request.id.toString())
        workManager().enqueueUniqueWork(
            Constants.TAG_GENERATION,
            ExistingWorkPolicy.REPLACE,
            request
        ).result.get()
        prunePayloads()
    }

    private fun prunePayloads() {
        val activeIds = workManager().getWorkInfosByTag(Constants.TAG_GENERATION).get()
            .filter { !it.state.isFinished }.mapTo(mutableSetOf()) { it.id.toString() }
        payloads.prune(activeIds)
    }
}
