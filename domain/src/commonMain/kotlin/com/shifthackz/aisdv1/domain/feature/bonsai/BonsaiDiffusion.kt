package com.shifthackz.aisdv1.domain.feature.bonsai

import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import kotlinx.coroutines.flow.Flow

/**
 * Cross-platform Bonsai generation boundary used by the shared repository.
 * Android submits work through the isolated inference gateway; iOS uses its registered MLX runtime.
 *
 * @author Dmitriy Moroz
 */
interface BonsaiDiffusion {
    /**
     * Generates with the captured payload and selected model resources.
     * Android returns one validated image per gateway attempt; the shared use case owns batch sequencing.
     *
     * @param payload Captured generation parameters and platform backend selection.
     * @param modelPath Downloaded or custom model directory selected before generation.
     * @return Base64-encoded generated image.
     * @author Dmitriy Moroz
     */
    suspend fun process(
        payload: TextToImagePayload,
        modelPath: String,
    ): String

    /**
     * Requests cancellation of the current platform operation.
     * Android delegates to the active native gateway; worker callers use owner-scoped cancellation instead.
     *
     * @author Dmitriy Moroz
     */
    suspend fun interrupt()

    /**
     * Observes Bonsai progress without initializing the model or native runtime in the Android host.
     *
     * @return Platform runtime counters; Android lifecycle stages are observed through the native gateway.
     * @author Dmitriy Moroz
     */
    fun observeStatus(): Flow<LocalDiffusionStatus>
}
