package com.shifthackz.aisdv1.domain.usecase.settings

/**
 * Selects an already owned MNN bundle without importing its external custom source again.
 *
 * @author Dmitriy Moroz
 */
interface SelectMnnModelUseCase {
    /**
     * Validates the captured owned snapshot and atomically replaces only MNN selection fields.
     *
     * @param modelId Catalog identity or the standard custom model identity.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(modelId: String): Result<Unit>
}
