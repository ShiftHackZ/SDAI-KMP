package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.gateway.MnnModelSelectionLease
import kotlinx.coroutines.flow.emptyFlow

/**
 * Explicit unsupported-platform binding; Android replaces it at composition time.
 *
 * @author Dmitriy Moroz
 */
internal object UnsupportedMnnGenerationRepository : MnnGenerationRepository {
    /**
     * Unsupported platforms never produce native progress.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStatus() = emptyFlow<LocalDiffusionStatus>()

    /**
     * Rejects MNN setup outside Android.
     *
     * @param modelId Requested catalog identity.
     * @param sourcePath Custom source that cannot be imported on this platform.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun validateModel(
        modelId: String,
        sourcePath: String
    ): MnnModelSelectionLease =
        error("MNN requires Android arm64")

    /**
     * Rejects owned-model selection on platforms without MNN.
     *
     * @param modelId Requested model identity.
     * @param activationId Captured snapshot manifest hash.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun validateOwnedModel(
        modelId: String,
        activationId: String
    ): MnnModelSelectionLease =
        error("MNN requires Android arm64")

    /**
     * Rejects deep-linked or stale MNN generation outside Android.
     *
     * @param payload Captured request.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun generateFromText(payload: TextToImagePayload): Nothing =
        error("MNN requires Android arm64")

    /**
     * No attempt can exist on unsupported platforms.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun interruptGeneration() = Unit
}
