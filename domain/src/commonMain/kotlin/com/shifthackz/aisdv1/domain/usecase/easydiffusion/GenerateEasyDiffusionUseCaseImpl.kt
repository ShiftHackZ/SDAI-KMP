package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionWorkOwner

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionFrame
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionHttpFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTask
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.EasyDiffusionTaskRegistry
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository
import kotlinx.coroutines.TimeoutCancellationException
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionSubmissionFailure
import kotlinx.coroutines.CancellationException
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTimeoutFailure
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import com.shifthackz.aisdv1.domain.usecase.generation.withGenerationMetadata
import com.shifthackz.aisdv1.domain.entity.GenerationMetadataContext
import kotlin.time.TimeSource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Owns remote queue/stream recovery and bounded task cleanup. A render POST is never replayed.
 *
 * @param repository Strict transport, endpoint-scoped catalog and normal result storage.
 * @param registry Serializes ownership across screen and worker callers.
 * @param preferences Deadline and source revision used to invalidate replaced work.
 * @author Dmitriy Moroz
 */
internal class GenerateEasyDiffusionUseCaseImpl(
    private val repository: EasyDiffusionRepository,
    private val registry: EasyDiffusionTaskRegistry,
    private val preferences: PreferenceManager,
) : GenerateEasyDiffusionUseCase {
    /**
     * Owns one sequential batch under a shared deadline, rejecting replaced connections and late results.
     * Caller cancellation remains cancellation; this operation's deadline is reported as a typed failure.
     *
     * @param request Immutable provider identity, model and supported generation inputs for this batch.
     * @return Completed results persisted through the application's normal gallery/export workflow.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(request: EasyDiffusionRequest): List<AiGenerationResult> {
        require(request.width > 0 && request.height > 0 && request.width % 8 == 0 && request.height % 8 == 0) {
            "Easy Diffusion dimensions must be positive multiples of eight"
        }
        require(request.steps > 0 && request.cfgScale.isFinite() && request.cfgScale >= 0f)
        require(request.seed in 0L..4294967295L && request.batchCount > 0)
        require(request.strength.isFinite() && request.strength in 0f..1f)
        check(request.model.isNotBlank()) { "Select an Easy Diffusion checkpoint" }
        requireOwner(request)
        val limit =
            preferences.generationRequestTimeout.seconds.let { if (it > 0) it.seconds else Duration.INFINITE }
        val started = TimeSource.Monotonic.markNow()
        registry.begin(
            request.sessionId,
            requireNotNull(coroutineContext[Job]),
            coroutineContext[EasyDiffusionWorkOwner]?.id
        )
        try {
            return withTimeout(limit) {
                coroutineScope {
                    val watcher = launch {
                        preferences.observe().first {
                            preferences.source != ServerSource.EASY_DIFFUSION ||
                                    preferences.providerSelectionRevision != request.selectionRevision ||
                                    preferences.easyDiffusionUrl != request.url
                        }
                        error("Easy Diffusion source changed; retry with the selected connection")
                    }
                    try {
                        repository.fetchModels(request.url)
                        check(repository.getModels(request.url).any { it.id == request.model }) {
                            "Easy Diffusion checkpoint is unavailable; refresh and select a model"
                        }
                        List(request.batchCount) { index ->
                            val seed =
                                if (index == 0) request.seed else (request.seed + index) % 4294967296L
                            withGenerationMetadata(
                                GenerationMetadataContext(
                                    ServerSource.EASY_DIFFUSION.key,
                                    "None",
                                    deviceName = coroutineContext[GenerationMetadataContext]?.deviceName,
                                    osVersion = coroutineContext[GenerationMetadataContext]?.osVersion,
                                )
                            ) {
                                generateSingle(
                                    request.copy(seed = seed, batchCount = 1),
                                    (limit - started.elapsedNow()).coerceAtLeast(1.milliseconds)
                                )
                            }
                        }
                    } finally {
                        watcher.cancel()
                    }
                }
            }
        } catch (failure: TimeoutCancellationException) {
            coroutineContext.ensureActive()
            throw EasyDiffusionTimeoutFailure(failure)
        } finally {
            withContext(NonCancellable) { registry.finish(request.sessionId) }
        }
    }

    /**
     * Waits for one task without replaying its POST and stops its captured identity on failure.
     *
     * @param request One image from the captured batch.
     * @param remaining Time left under the batch deadline, including candidate validation and queue waits.
     * @author Dmitriy Moroz
     */
    private suspend fun generateSingle(
        request: EasyDiffusionRequest,
        remaining: Duration
    ): AiGenerationResult {
        var task: EasyDiffusionTask? = null
        var hasCompleted = false
        try {
            requireOwner(request)
            registry.update(request.sessionId, EasyDiffusionProgress(isRunning = true))
            // Recover an in-flight task identity after cancellation without exceeding either configured deadline.
            // If both deadlines are disabled, bound only this uncancellable ownership-recovery wait to two minutes.
            val connectionLimit =
                preferences.connectionTimeout.seconds.let { if (it > 0) it.seconds else Duration.INFINITE }
            val submitLimit =
                minOf(connectionLimit, remaining).let { if (it.isInfinite()) 120.seconds else it }
            try {
                withContext(NonCancellable) {
                    withTimeout(submitLimit) {
                        try {
                            task = repository.submit(request)
                        } catch (failure: CancellationException) {
                            throw failure
                        } catch (failure: EasyDiffusionHttpFailure) {
                            throw failure
                        } catch (failure: Exception) {
                            throw EasyDiffusionSubmissionFailure(failure)
                        }
                    }
                }
            } catch (failure: Exception) {
                // Submission recovery cannot replace cancellation of the original caller.
                coroutineContext.ensureActive()
                throw failure
            }
            requireOwner(request)
            var terminal: EasyDiffusionFrame? = null
            while (terminal == null) {
                requireOwner(request)
                try {
                    repository.readStream(requireNotNull(task)) { frame ->
                        requireOwner(request)
                        check(terminal == null) { "Easy Diffusion returned data after completion" }
                        when (frame.status) {
                            "succeeded" -> terminal = frame
                            "failed", "stopped", "cancelled" -> error("Easy Diffusion generation failed; check the server")
                            null -> {
                                check(frame.detail == null) { "Easy Diffusion generation failed; check the server" }
                                registry.update(
                                    request.sessionId,
                                    EasyDiffusionProgress(frame.step, frame.totalSteps, true)
                                )
                            }

                            else -> error("Easy Diffusion returned an unknown terminal status")
                        }
                    }
                } catch (failure: EasyDiffusionHttpFailure) {
                    if (failure.statusCode != 425) throw failure
                }
                if (terminal == null) {
                    delay(500)
                    val status =
                        repository.ping(requireNotNull(task).url, requireNotNull(task).sessionId)
                    check(
                        status in setOf(
                            "Online",
                            "Rendering",
                            "LoadingModel"
                        )
                    ) { "Easy Diffusion is unavailable; retry when ready" }
                }
            }
            requireOwner(request)
            val result = repository.save(request, requireNotNull(terminal))
            hasCompleted = true
            return result
        } finally {
            val ownedTask = task
            if (!hasCompleted && ownedTask != null) {
                withContext(NonCancellable) {
                    try {
                        withTimeout(5.seconds) { repository.stop(ownedTask) }
                    } catch (failure: Exception) {
                        // Preserve the original failure/cancellation. Cleanup is best effort and never global.
                        if (failure is EasyDiffusionHttpFailure && failure.statusCode in setOf(
                                404,
                                409
                            )
                        ) Unit
                    }
                }
            }
        }
    }

    /**
     * Prevents dispatch or publication after source replacement or cancellation.
     *
     * @param request Submitted snapshot whose revision must still be selected.
     * @author Dmitriy Moroz
     */
    private suspend fun requireOwner(request: EasyDiffusionRequest) {
        coroutineContext.ensureActive()
        check(
            preferences.source == ServerSource.EASY_DIFFUSION &&
                    preferences.providerSelectionRevision == request.selectionRevision &&
                    preferences.easyDiffusionUrl == request.url
        ) { "Generation source changed" }
    }
}
