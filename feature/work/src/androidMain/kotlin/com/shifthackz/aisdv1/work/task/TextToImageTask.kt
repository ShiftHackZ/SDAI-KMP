package com.shifthackz.aisdv1.work.task

import com.shifthackz.aisdv1.domain.gateway.RemoteGenerationProgressGateway

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.ObserveEasyDiffusionProgressUseCase

import android.content.Context
import com.shifthackz.aisdv1.work.GenerationWorkPayloadCleanup
import com.shifthackz.aisdv1.work.GenerationWorkPayloadStore
import com.shifthackz.aisdv1.domain.entity.NativeGenerationOwner
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionWorkOwner
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import kotlinx.coroutines.withContext
import androidx.work.WorkerParameters
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.common.log.debugLog
import com.shifthackz.aisdv1.core.common.log.errorLog
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptWorkerGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import com.shifthackz.aisdv1.work.Constants
import com.shifthackz.aisdv1.work.Constants.NOTIFICATION_TEXT_TO_IMAGE_FOREGROUND
import com.shifthackz.aisdv1.work.Constants.NOTIFICATION_TEXT_TO_IMAGE_GENERIC
import com.shifthackz.aisdv1.work.core.CoreGenerationWorker
import com.shifthackz.aisdv1.work.mappers.toTextToImagePayload
import kotlinx.coroutines.CancellationException

/**
 * Background txt2img worker.
 *
 * The task reads the cached generation payload, starts provider-specific
 * progress observation, executes txt2img generation, and reports success,
 * cancellation, or failure through the foreground notification path.
 * Native attempts, including Bonsai, are never automatically replayed after interruption.
 *
 * @param context Worker application context.
 * @param workerParameters Durable task identity and retry count.
 * @param pushNotificationManager Publishes foreground and terminal notifications.
 * @param activityIntentProvider Opens the app from notifications.
 * @param remoteGenerationProgressGateway Owner-scoped remote job/workflow measurements.
 * @param observeEasyDiffusionProgressUseCase Hot queued/sampling progress for this remote generation owner.
 * @param observeHordeProcessStatusUseCase Supplies remote queue progress.
 * @param interruptWorkerGenerationUseCase Cancels only native work owned by this UUID.
 * @param preferenceManager Supplies the source for legacy payloads without a snapshot.
 * @param backgroundWorkObserver Owns current background presentation state.
 * @param textToImageUseCase Routes immutable inputs and sequential batches.
 * @param payloads Reads the immutable work request.
 * @param payloadCleanup Retains retry input and cleans up terminal request files.
 * @param nativeGateway Observes the child and preserves per-attempt ownership.
 * @author Dmitriy Moroz
 */
internal class TextToImageTask(
    context: Context,
    workerParameters: WorkerParameters,
    pushNotificationManager: PushNotificationManager,
    activityIntentProvider: ActivityIntentProvider,
    observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    remoteGenerationProgressGateway: RemoteGenerationProgressGateway,
    observeEasyDiffusionProgressUseCase: ObserveEasyDiffusionProgressUseCase,
    interruptWorkerGenerationUseCase: InterruptWorkerGenerationUseCase,
    private val preferenceManager: PreferenceManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val textToImageUseCase: TextToImageUseCase,
    private val payloads: GenerationWorkPayloadStore,
    private val payloadCleanup: GenerationWorkPayloadCleanup,
    nativeGateway: NativeGenerationGateway,
) : CoreGenerationWorker(
    context = context,
    nativeGateway = nativeGateway,
    workerParameters = workerParameters,
    pushNotificationManager = pushNotificationManager,
    activityIntentProvider = activityIntentProvider,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
    observeHordeProcessStatusUseCase = observeHordeProcessStatusUseCase,
    remoteGenerationProgressGateway = remoteGenerationProgressGateway,
    observeEasyDiffusionProgressUseCase = observeEasyDiffusionProgressUseCase,
    interruptWorkerGenerationUseCase = interruptWorkerGenerationUseCase,
) {

    override val notificationId: Int = NOTIFICATION_TEXT_TO_IMAGE_FOREGROUND

    override val genericNotificationId: Int = NOTIFICATION_TEXT_TO_IMAGE_GENERIC

    /**
     * Runs one captured request with worker ownership; interrupted native work requires explicit retry.
     * Cancellation and failure clear subscriptions and retain input according to payload cleanup policy.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun doWork(): Result {
        if (!backgroundWorkObserver.publishForOwner(id.toString()) {
                backgroundWorkObserver.refreshStatus()
                backgroundWorkObserver.dismissResult()
            }) return Result.failure()
        handleStart()
        debugLog("Starting TextToImageTask!")

        return try {
            val bytes = payloads.read(
                id.toString(),
                Constants.FILE_TEXT_TO_IMAGE,
                inputData.getBoolean(Constants.IMMUTABLE_PAYLOAD, false)
            )
            val payload = bytes.toTextToImagePayload()

            if (payload == null) {
                val t = Throwable("Payload is null.")
                handleError(t)
                clearSubscriptions()
                errorLog(t, "Payload was failed to read/parse.")
                return Result.failure()
            }

            source = payload.sourceSnapshot ?: source
            if (runAttemptCount > 0 && source in setOf(
                    ServerSource.EASY_DIFFUSION,
                    ServerSource.LOCAL_MICROSOFT_ONNX,
                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                    ServerSource.LOCAL_APPLE_BONSAI,
                    ServerSource.LOCAL_STABLE_DIFFUSION_CPP
                )
            ) {
                error("Interrupted native work requires an explicit retry.")
            }
            listenSourceStatus()

            handleProcess()
            runCatching {
                withContext(NativeGenerationOwner(id.toString()) + EasyDiffusionWorkOwner(id.toString())) {
                    textToImageUseCase(
                        payload
                    )
                }
            }
                .fold(
                    onSuccess = { result ->
                        handleSuccess(result)
                        debugLog("Generation finished successfully!")
                        Result.success()
                    },
                    onFailure = { t ->
                        if (t is CancellationException) throw t
                        handleError(t)
                        errorLog(t, "Caught exception from TextToImageUseCase!")
                        Result.failure()
                    },
                )
        } catch (e: CancellationException) {
            handleCancellation()
            throw e
        } catch (e: Exception) {
            handleError(e)
            errorLog(e, "Caught exception from TextToImageTask worker!")
            Result.failure()
        } finally {
            clearSubscriptions()
            payloadCleanup.complete(id, isStopped)
        }
    }
}
