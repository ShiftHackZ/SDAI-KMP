package com.shifthackz.aisdv1.work.task

import android.content.Context
import com.shifthackz.aisdv1.work.GenerationWorkPayloadCleanup
import com.shifthackz.aisdv1.work.GenerationWorkPayloadStore
import com.shifthackz.aisdv1.domain.entity.NativeGenerationOwner
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import kotlinx.coroutines.withContext
import androidx.work.WorkerParameters
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.log.debugLog
import com.shifthackz.aisdv1.core.common.log.errorLog
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptWorkerGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import com.shifthackz.aisdv1.work.Constants
import com.shifthackz.aisdv1.work.Constants.NOTIFICATION_TEXT_TO_IMAGE_FOREGROUND
import com.shifthackz.aisdv1.work.Constants.NOTIFICATION_TEXT_TO_IMAGE_GENERIC
import com.shifthackz.aisdv1.work.core.CoreGenerationWorker
import com.shifthackz.aisdv1.work.mappers.toTextToImagePayload
import kotlinx.coroutines.CancellationException
import java.io.File

/**
 * Background txt2img worker.
 *
 * The task reads the cached generation payload, starts provider-specific
 * progress observation, executes txt2img generation, and reports success,
 * cancellation, or failure through the foreground notification path.
 */
internal class TextToImageTask(
    context: Context,
    workerParameters: WorkerParameters,
    pushNotificationManager: PushNotificationManager,
    activityIntentProvider: ActivityIntentProvider,
    observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    observeBonsaiProcessStatusUseCase: ObserveBonsaiProcessStatusUseCase,
    interruptWorkerGenerationUseCase: InterruptWorkerGenerationUseCase,
    private val preferenceManager: PreferenceManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val textToImageUseCase: TextToImageUseCase,
    private val fileProviderDescriptor: FileProviderDescriptor,
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
    observeLocalDiffusionProcessStatusUseCase = observeLocalDiffusionProcessStatusUseCase,
    observeBonsaiProcessStatusUseCase = observeBonsaiProcessStatusUseCase,
    interruptWorkerGenerationUseCase = interruptWorkerGenerationUseCase,
) {

    override val notificationId: Int = NOTIFICATION_TEXT_TO_IMAGE_FOREGROUND

    override val genericNotificationId: Int = NOTIFICATION_TEXT_TO_IMAGE_GENERIC

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
                    ServerSource.LOCAL_MICROSOFT_ONNX,
                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                    ServerSource.LOCAL_STABLE_DIFFUSION_CPP
                )
            ) {
                error("Interrupted native work requires an explicit retry.")
            }
            listenSourceStatus()

            handleProcess()
            runCatching {
                withContext(NativeGenerationOwner(id.toString())) {
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
