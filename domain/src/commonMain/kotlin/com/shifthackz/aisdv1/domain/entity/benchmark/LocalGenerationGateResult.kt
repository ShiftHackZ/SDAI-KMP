package com.shifthackz.aisdv1.domain.entity.benchmark


/**
 * Decision returned before starting local generation.
 *
 * @author Dmitriy Moroz
 */
sealed interface LocalGenerationGateResult {
    data object Ready : LocalGenerationGateResult
    data object AskRunBenchmark : LocalGenerationGateResult
    data class ConfirmExceedsRecommendation(
        val reasons: List<BenchmarkLimitExceeded>,
        val recommendation: BenchmarkRecommendation,
    ) : LocalGenerationGateResult
}
