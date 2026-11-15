package com.shifthackz.aisdv1.work.task

import android.content.Context
import com.shifthackz.aisdv1.work.GenerationWorkPayloadCleanup
import com.shifthackz.aisdv1.work.GenerationWorkPayloadStore
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import androidx.work.WorkerParameters
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptWorkerGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.work.Constants
import com.shifthackz.aisdv1.work.Constants.NOTIFICATION_IMAGE_TO_IMAGE_FOREGROUND
import com.shifthackz.aisdv1.work.Constants.NOTIFICATION_IMAGE_TO_IMAGE_GENERIC
import com.shifthackz.aisdv1.work.core.CoreGenerationWorker
import com.shifthackz.aisdv1.work.mappers.toImageToImagePayload
import kotlinx.coroutines.CancellationException
import java.io.File

/**
 * Background img2img worker.
 *
 * The task reads the cached serialized payload, subscribes to provider progress,
 * runs the image-to-image use case, and publishes the result or failure through
 * the common generation notification flow.
 */
internal class ImageToImageTask(
    context: Context,
    workerParameters: WorkerParameters,
    pushNotificationManager: PushNotificationManager,
    activityIntentProvider: ActivityIntentProvider,
    preferenceManager: PreferenceManager,
    observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    observeBonsaiProcessStatusUseCase: ObserveBonsaiProcessStatusUseCase,
    interruptWorkerGenerationUseCase: InterruptWorkerGenerationUseCase,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val imageToImageUseCase: ImageToImageUseCase,
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

    override val notificationId = NOTIFICATION_IMAGE_TO_IMAGE_FOREGROUND

    override val genericNotificationId = NOTIFICATION_IMAGE_TO_IMAGE_GENERIC

    override suspend fun doWork(): Result {
        if (!backgroundWorkObserver.publishForOwner(id.toString()) {
                backgroundWorkObserver.refreshStatus()
                backgroundWorkObserver.dismissResult()
            }) return Result.failure()
        handleStart()
        return try {
            val bytes = payloads.read(
                id.toString(),
                Constants.FILE_IMAGE_TO_IMAGE,
                inputData.getBoolean(Constants.IMMUTABLE_PAYLOAD, false)
            )
            val payload = bytes.toImageToImagePayload()

            if (payload == null) {
                handleError(Throwable("Payload is null."))
                clearSubscriptions()
                return Result.failure()
            }

            listenSourceStatus()

            handleProcess()
            runCatching { imageToImageUseCase(payload) }
                .fold(
                    onSuccess = { result ->
                        handleSuccess(result)
                        Result.success()
                    },
                    onFailure = { t ->
                        if (t is CancellationException) throw t
                        handleError(t)
                        Result.failure()
                    },
                )
        } catch (e: CancellationException) {
            handleCancellation()
            throw e
        } catch (e: Exception) {
            handleError(e)
            Result.failure()
        } finally {
            clearSubscriptions()
            payloadCleanup.complete(id, isStopped)
        }
    }
}
