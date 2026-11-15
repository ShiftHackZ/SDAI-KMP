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
 * Bonsai remains text-to-image only and is not dispatched by this worker.
 *
 * @param context Worker application context.
 * @param workerParameters Durable WorkManager task identity.
 * @param pushNotificationManager Publishes foreground and terminal notifications.
 * @param activityIntentProvider Opens the app from notifications.
 * @param preferenceManager Supplies the source for legacy work.
 * @param observeHordeProcessStatusUseCase Supplies remote queue progress.
 * @param observeLocalDiffusionProcessStatusUseCase Supplies legacy runtime progress.
 * @param interruptWorkerGenerationUseCase Cancels captured source work for this worker.
 * @param backgroundWorkObserver Owns background presentation state.
 * @param imageToImageUseCase Routes the existing image-to-image request.
 * @param fileProviderDescriptor Legacy work file location descriptor.
 * @param payloads Reads immutable work input.
 * @param payloadCleanup Cleans up terminal request files and retains retry input.
 * @param nativeGateway Shared native lifecycle dependency of the worker base.
 * @author Dmitriy Moroz
 */
internal class ImageToImageTask(
    context: Context,
    workerParameters: WorkerParameters,
    pushNotificationManager: PushNotificationManager,
    activityIntentProvider: ActivityIntentProvider,
    preferenceManager: PreferenceManager,
    observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
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
