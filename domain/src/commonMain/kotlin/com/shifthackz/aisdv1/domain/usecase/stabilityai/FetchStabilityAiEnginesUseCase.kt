package com.shifthackz.aisdv1.domain.usecase.stabilityai

import com.shifthackz.aisdv1.domain.entity.StabilityAiEngine
import com.shifthackz.aisdv1.domain.repository.StabilityAiEnginesRepository

/**
 * Loads the engines available to a Stability AI account.
 *
 * @author Dmitriy Moroz
 */
interface FetchStabilityAiEnginesUseCase {

    /**
     * Returns the engines available to the authenticated account.
     *
     * @param apiKey Account API key used to authenticate the engine request.
     * @return The catalog available for the current provider.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(apiKey: String): List<StabilityAiEngine>
}

/**
 * Loads the Stability AI engines available to the supplied account.
 *
 * @param repository Fetches engine descriptors using the supplied API key.
 *
 * @author Dmitriy Moroz
 */
internal class FetchStabilityAiEnginesUseCaseImpl(
    private val repository: StabilityAiEnginesRepository,
) : FetchStabilityAiEnginesUseCase {

    /**
     * Returns the engines available to the authenticated account.
     *
     * @param apiKey Account API key used to authenticate the engine request.
     * @return The catalog available for the current provider.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(apiKey: String): List<StabilityAiEngine> =
        repository.fetch(apiKey)
}
