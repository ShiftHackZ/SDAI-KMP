package com.shifthackz.aisdv1.work.fixture

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import com.shifthackz.aisdv1.domain.entity.HordeProcessStatus
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgress
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.gateway.RemoteGenerationProgressGateway
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.ObserveEasyDiffusionProgressUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptSourceGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveCoreMlProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveMnnProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import com.shifthackz.aisdv1.work.BackgroundTaskManagerImpl
import com.shifthackz.aisdv1.work.BackgroundWorkObserverImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Holds real domain payloads at controllable generation and progress boundaries.
 * Separate status jobs let a scenario cancel subscriptions without cancelling its own test scope.
 *
 * @param scope Application lifetime substituted with the scenario's deterministic scheduler.
 * @author Dmitriy Moroz
 */
internal class IosGenerationFixture(scope: CoroutineScope) {
    val execution = RecordingIosWorkExecution()
    val observer = BackgroundWorkObserverImpl()
    val preferences = GenerationTestPreferences()
    val textRequests = mutableListOf<TextToImagePayload>()
    val imageRequests = mutableListOf<ImageToImagePayload>()
    val results = mutableListOf<CompletableDeferred<List<AiGenerationResult>>>()
    val interruptions = mutableListOf<ServerSource>()
    var cleanupBarrier: CompletableDeferred<Unit>? = null
    val remote = MutableStateFlow<List<RemoteGenerationProgress>>(emptyList())
    val easy = MutableSharedFlow<EasyDiffusionProgress>(extraBufferCapacity = 8)
    val local = MutableSharedFlow<LocalDiffusionStatus>(extraBufferCapacity = 8)

    val manager = BackgroundTaskManagerImpl(
        observer, execution,
        object : RemoteGenerationProgressGateway {
            override fun observe() = remote
            override fun update(taskId: String, progress: RemoteGenerationProgress?) {
                error("Generation manager must only observe remote progress")
            }
        },
        object : TextToImageUseCase {
            override suspend fun invoke(payload: TextToImagePayload): List<AiGenerationResult> {
                textRequests += payload
                return awaitResult()
            }
        },
        object : ImageToImageUseCase {
            override suspend fun invoke(payload: ImageToImagePayload): List<AiGenerationResult> {
                imageRequests += payload
                return awaitResult()
            }
        },
        object : InterruptSourceGenerationUseCase {
            override suspend fun invoke(source: ServerSource) {
                interruptions += source
            }
        },
        object : ObserveHordeProcessStatusUseCase {
            override fun invoke() = emptyFlow<HordeProcessStatus>()
        },
        object : ObserveLocalDiffusionProcessStatusUseCase {
            override fun invoke() = local
        },
        object : ObserveCoreMlProcessStatusUseCase {
            override fun invoke() = local
        },
        object : ObserveBonsaiProcessStatusUseCase {
            override fun invoke() = local
        },
        object : ObserveMnnProcessStatusUseCase {
            override fun invoke() = local
        },
        preferences,
        object : ObserveEasyDiffusionProgressUseCase {
            override fun invoke() = easy
        },
        scope,
        CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext[Job])),
    )

    /**
     * Delays results and optionally retains cancellation cleanup after the generation coroutine stops.
     *
     * @author Dmitriy Moroz
     */
    private suspend fun awaitResult(): List<AiGenerationResult> = try {
        CompletableDeferred<List<AiGenerationResult>>().also { results += it }.await()
    } finally {
        withContext(NonCancellable) { cleanupBarrier?.await() }
    }
}
