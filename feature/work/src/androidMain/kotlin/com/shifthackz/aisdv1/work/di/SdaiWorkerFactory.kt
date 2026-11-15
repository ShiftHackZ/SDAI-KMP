package com.shifthackz.aisdv1.work.di

import android.content.Context
import com.shifthackz.aisdv1.work.GenerationWorkPayloadCleanup
import com.shifthackz.aisdv1.work.GenerationWorkPayloadStore
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
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
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import com.shifthackz.aisdv1.work.task.ImageToImageTask
import com.shifthackz.aisdv1.work.task.TextToImageTask

/**
 * WorkManager factory that wires SDAI generation workers with app services.
 *
 * The factory keeps foreground notification dependencies and local-generation
 * progress observers in one place so background txt2img/img2img tasks use the
 * same runtime status pipeline as foreground generation.
 */
class SdaiWorkerFactory(
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val pushNotificationManager: PushNotificationManager,
    private val preferenceManager: PreferenceManager,
    private val textToImageUseCase: TextToImageUseCase,
    private val imageToImageUseCase: ImageToImageUseCase,
    private val observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    private val observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    private val observeBonsaiProcessStatusUseCase: ObserveBonsaiProcessStatusUseCase,
    private val interruptWorkerGenerationUseCase: InterruptWorkerGenerationUseCase,
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val activityIntentProvider: ActivityIntentProvider,
    private val payloads: GenerationWorkPayloadStore,
    private val nativeGateway: NativeGenerationGateway,
    private val payloadCleanup: GenerationWorkPayloadCleanup,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
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
                observeLocalDiffusionProcessStatusUseCase = observeLocalDiffusionProcessStatusUseCase,
                observeBonsaiProcessStatusUseCase = observeBonsaiProcessStatusUseCase,
                interruptWorkerGenerationUseCase = interruptWorkerGenerationUseCase,
                fileProviderDescriptor = fileProviderDescriptor,
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
                observeLocalDiffusionProcessStatusUseCase = observeLocalDiffusionProcessStatusUseCase,
                observeBonsaiProcessStatusUseCase = observeBonsaiProcessStatusUseCase,
                interruptWorkerGenerationUseCase = interruptWorkerGenerationUseCase,
                fileProviderDescriptor = fileProviderDescriptor,
            )

            else -> null
        }
    }
}
