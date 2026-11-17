package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.gateway.MnnModelSelectionLease
import kotlinx.coroutines.flow.Flow

/**
 * Host gateway facade for isolated Android MNN inference and validated model selection.
 *
 * @author Dmitriy Moroz
 */
interface MnnGenerationRepository {
    /**
     * Reports overall upstream percentage while the engine remains in RUNNING.
     *
     * @author Dmitriy Moroz
     */
    fun observeStatus(): Flow<LocalDiffusionStatus>

    /**
     * Prepares a complete catalog or custom bundle and returns a lease retained through configuration persistence.
     *
     * @param modelId Catalog identity, never an arbitrary filesystem path.
     * @param sourcePath External custom bundle source selected by setup; empty for downloaded identities.
     *
     * @author Dmitriy Moroz
     */
    suspend fun validateModel(modelId: String, sourcePath: String): MnnModelSelectionLease

    /**
     * Protects an already owned snapshot through configuration commit without reading or copying its external source.
     *
     * @param modelId Catalog or custom UI identity.
     * @param activationId Captured manifest hash; required for custom snapshots.
     *
     * @author Dmitriy Moroz
     */
    suspend fun validateOwnedModel(modelId: String, activationId: String): MnnModelSelectionLease

    /**
     * Runs the captured bundle/backend with a lease lasting until the child terminates.
     *
     * @param payload Immutable supported SD 1.5 inputs and model identity.
     *
     * @author Dmitriy Moroz
     */
    suspend fun generateFromText(payload: TextToImagePayload): AiGenerationResult

    /**
     * Cancels the currently admitted foreground attempt without destroying native resources concurrently.
     *
     * @author Dmitriy Moroz
     */
    suspend fun interruptGeneration()
}
