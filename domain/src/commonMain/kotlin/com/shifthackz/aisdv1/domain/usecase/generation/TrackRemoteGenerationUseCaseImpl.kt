package com.shifthackz.aisdv1.domain.usecase.generation

import kotlin.uuid.ExperimentalUuidApi

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionWorkOwner
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgress
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgressContext
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.RemoteGenerationProgressGateway
import com.shifthackz.aisdv1.domain.repository.Automatic1111ProgressRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.uuid.Uuid

/**
 * Keeps telemetry optional while generation failures and caller cancellation retain their normal semantics.
 * @param gateway Shared owner-scoped measurements for foreground and worker UI.
 * @param repository Strict AUTOMATIC1111 progress reads.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalUuidApi::class)
internal class TrackRemoteGenerationUseCaseImpl(
    private val gateway: RemoteGenerationProgressGateway,
    private val repository: Automatic1111ProgressRepository,
) : TrackRemoteGenerationUseCase {
    /**
     * Polls AUTOMATIC1111 at one-second intervals or accepts request-scoped SwarmUI workflow updates.
     * A failed telemetry request clears the measurement and stops polling without replaying generation.
     * @param source Captured remote provider.
     * @param block Normal generation operation.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(
        source: ServerSource,
        block: suspend () -> List<AiGenerationResult>
    ): List<AiGenerationResult> = coroutineScope {
        val taskId = "task(sdai-${Uuid.random()})"
        val ownerId = coroutineContext[EasyDiffusionWorkOwner]?.id
        val job = coroutineContext[Job]
        val context = RemoteGenerationProgressContext(taskId) { fraction ->
            if (job?.isActive == true) gateway.update(
                taskId, RemoteGenerationProgress(
                    source,
                    ownerId,
                    fraction?.takeIf { it.isFinite() }?.coerceIn(0f, 1f)?.times(100)?.toInt()
                )
            )
        }
        context.report(null)
        val poller = if (source == ServerSource.AUTOMATIC1111) launch {
            try {
                while (true) {
                    delay(1000)
                    context.report(repository.getProgress(taskId))
                }
            } catch (failure: CancellationException) {
                throw failure
            } catch (_: Exception) {
                context.report(null)
            }
        } else null
        try {
            withContext(context) { block() }
        } finally {
            poller?.cancel()
            gateway.update(taskId, null)
        }
    }
}
