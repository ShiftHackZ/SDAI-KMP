package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationRequest
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationGateResult

/**
 * Applies persisted benchmark recommendation policy to local requests.
 *
 * @author Dmitriy Moroz
 */
interface LocalGenerationBenchmarkGate {
    /**
     * Assesses admission without starting generation.
     *
     * @param request Generation parameters to compare with the saved recommendation.
     * @author Dmitriy Moroz
     */
    suspend fun evaluate(request: LocalGenerationRequest): LocalGenerationGateResult

    /**
     * Records that the initial benchmark question was answered.
     *
     * @author Dmitriy Moroz
     */
    fun markFirstBenchmarkPromptAnswered()

    /**
     * Suppresses parameter warnings until another benchmark runs.
     *
     * @author Dmitriy Moroz
     */
    fun suppressRecommendationWarnings()
}
