package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.MnnBackend

/**
 * Selects a validated downloaded MNN bundle and independently persisted backend.
 *
 * @author Dmitriy Moroz
 */
interface ConnectToMnnUseCase {
    /**
     * Commits provider selection only after complete bundle validation succeeds.
     *
     * @param modelId Downloaded catalog identity.
     * @param modelPath External folder used only for the standard custom-model identity.
     * @param backend Requested CPU or OpenCL backend, checked again during inference.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(
        modelId: String,
        modelPath: String,
        backend: MnnBackend
    ): Result<Unit>
}
