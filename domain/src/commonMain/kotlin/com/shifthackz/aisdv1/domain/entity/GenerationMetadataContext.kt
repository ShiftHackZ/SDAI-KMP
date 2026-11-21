package com.shifthackz.aisdv1.domain.entity

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Carries immutable request provenance and a monotonic start through provider dispatch to persistence.
 * Child coroutines inherit it; sequential images receive a fresh context, while native batches share one.
 *
 * @param providerKey Provider selected when this operation was admitted.
 * @param acceleration Execution configuration captured from the submitted request.
 * @param deviceName Originating device name and model captured before dispatch.
 * @param osVersion Originating operating system captured before dispatch.
 * @param started Monotonic operation start, independent of wall-clock and timezone changes.
 * @author Dmitriy Moroz
 */
class GenerationMetadataContext(
    val providerKey: String,
    val acceleration: String?,
    val deviceName: String? = null,
    val osVersion: String? = null,
    private val started: TimeMark = TimeSource.Monotonic.markNow(),
) : AbstractCoroutineContextElement(Key) {
    private val completionMutex = Mutex()
    private var completedDurationMillis: Long? = null

    /**
     * Freezes provenance before gallery insertion or export begins. Images returned by one provider
     * batch share the first completion duration, excluding earlier images’ persistence/export time.
     *
     * @param result Completed provider image whose transient guards must be preserved.
     * @return Image with captured provider, execution mode and elapsed milliseconds.
     * @author Dmitriy Moroz
     */
    suspend fun completed(result: AiGenerationResult): AiGenerationResult =
        completionMutex.withLock {
            val durationMillis = completedDurationMillis ?: started.elapsedNow().inWholeMilliseconds
                .coerceAtLeast(0).also { completedDurationMillis = it }
            result.copy(
                providerKey = providerKey,
                deviceName = deviceName,
                osVersion = osVersion,
                acceleration = result.acceleration ?: acceleration,
                generationDurationMillis = durationMillis,
            )
        }

    /**
     * Coroutine lookup key for the currently admitted image operation.
     *
     * @author Dmitriy Moroz
     */
    companion object Key : CoroutineContext.Key<GenerationMetadataContext>
}
