package com.shifthackz.aisdv1.domain.usecase.settings

/**
 * Validates and commits independent upscaler configuration.
 *
 * @author Dmitriy Moroz
 */
interface ConfigureUpscalerUseCase {
    /**
     * Selects an available downloaded checkpoint without changing generation configuration.
     *
     * @param modelId Dedicated upscaler catalog identity.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(modelId: String)
}
