package com.shifthackz.aisdv1.work.di

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.ObserveEasyDiffusionProgressUseCase

import android.content.Context
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelsUseCase
import com.shifthackz.aisdv1.work.task.DownloadModelsTask
import com.shifthackz.aisdv1.work.GenerationWorkPayloadCleanup
import com.shifthackz.aisdv1.work.GenerationWorkPayloadStore
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptWorkerGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import com.shifthackz.aisdv1.work.task.ImageToImageTask
import com.shifthackz.aisdv1.work.task.TextToImageTask

/**
 * WorkManager factory that wires SDAI generation workers with app services.
 *
 * The factory keeps foreground notification dependencies and local-generation
 * progress observers in one place so background txt2img/img2img tasks use the
 * same runtime status pipeline as foreground generation.
 *
 * @param backgroundWorkObserver Owns current background presentation state.
 * @param pushNotificationManager Publishes worker notifications.
 * @param preferenceManager Supplies legacy source and cancellation settings.
 * @param textToImageUseCase Executes captured text-to-image requests.
 * @param imageToImageUseCase Executes existing image-to-image requests.
 * @param observeEasyDiffusionProgressUseCase Hot queued/sampling progress for this remote generation owner.
 * @param observeHordeProcessStatusUseCase Supplies remote queue progress.
 * @param interruptWorkerGenerationUseCase Cancels only work owned by the requested worker.
 * @param activityIntentProvider Creates app navigation intents for notifications.
 * @param payloads Reads immutable work files.
 * @param nativeGateway Shared child-process status and cancellation gateway.
 * @param downloadModels Sequential model download policy for foreground work.
 * @param payloadCleanup Cleans terminal request files and retains retry input.
 * @author Dmitriy Moroz
 */
class SdaiWorkerFactory(
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val pushNotificationManager: PushNotificationManager,
    private val preferenceManager: PreferenceManager,
    private val textToImageUseCase: TextToImageUseCase,
    private val imageToImageUseCase: ImageToImageUseCase,
    private val observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    private val observeEasyDiffusionProgressUseCase: ObserveEasyDiffusionProgressUseCase,
    private val interruptWorkerGenerationUseCase: InterruptWorkerGenerationUseCase,
    private val activityIntentProvider: ActivityIntentProvider,
    private val payloads: GenerationWorkPayloadStore,
    private val nativeGateway: NativeGenerationGateway,
    private val payloadCleanup: GenerationWorkPayloadCleanup,
    private val downloadModels: DownloadModelsUseCase,
) : WorkerFactory() {

    /**
     * Constructs supported workers with the shared native gateway; unknown classes use other factories.
     *
     * @param appContext WorkManager application context.
     * @param workerClassName Fully qualified worker class name supplied by WorkManager.
     * @param workerParameters Durable worker identity and input parameters.
     * @author Dmitriy Moroz
     */
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            DownloadModelsTask::class.java.name -> DownloadModelsTask(
                appContext,
                workerParameters,
                pushNotificationManager,
                activityIntentProvider,
                downloadModels
            )

            TextToImageTask::class.java.name -> TextToImageTask(
                context = appContext,
                payloads = payloads,
                payloadCleanup = payloadCleanup,
                nativeGateway = nativeGateway,
                workerParameters = workerParameters,
                pushNotificationManager = pushNotificationManager,
                activityIntentProvider = activityIntentProvider,
                backgroundWorkObserver = backgroundWorkObserver,
                preferenceManager = preferenceManager,
                textToImageUseCase = textToImageUseCase,
                observeHordeProcessStatusUseCase = observeHordeProcessStatusUseCase,
                observeEasyDiffusionProgressUseCase = observeEasyDiffusionProgressUseCase,
                interruptWorkerGenerationUseCase = interruptWorkerGenerationUseCase,
            )

            ImageToImageTask::class.java.name -> ImageToImageTask(
                context = appContext,
                payloads = payloads,
                payloadCleanup = payloadCleanup,
                nativeGateway = nativeGateway,
                workerParameters = workerParameters,
                pushNotificationManager = pushNotificationManager,
                activityIntentProvider = activityIntentProvider,
                backgroundWorkObserver = backgroundWorkObserver,
                preferenceManager = preferenceManager,
                imageToImageUseCase = imageToImageUseCase,
                observeHordeProcessStatusUseCase = observeHordeProcessStatusUseCase,
                observeEasyDiffusionProgressUseCase = observeEasyDiffusionProgressUseCase,
                interruptWorkerGenerationUseCase = interruptWorkerGenerationUseCase,
            )

            else -> null
        }
    }
}
