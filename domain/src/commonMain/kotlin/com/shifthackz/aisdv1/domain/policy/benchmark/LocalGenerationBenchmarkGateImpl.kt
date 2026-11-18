package com.shifthackz.aisdv1.domain.policy.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkRecommendation
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkProviderRecommendation
import com.shifthackz.aisdv1.domain.entity.benchmark.providerRecommendation
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationRequest
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkLimitExceeded
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationGateResult
import com.shifthackz.aisdv1.domain.entity.benchmark.isLocalGenerationSource
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.BenchmarkRepository
import com.shifthackz.aisdv1.domain.gateway.LocalGenerationBenchmarkGate
import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Chooses whether a captured request can proceed or needs a benchmark/recommendation dialog.
 * Remote providers bypass the policy. A saved benchmark answers the initial prompt implicitly;
 * warning suppression bypasses subsequent recommendation comparisons. Storage failures propagate.
 *
 * @param preferenceManager Reads/writes the initial-question and warning-suppression preferences.
 * @param repository Supplies the latest persistent recommendation used to compare local request limits.
 *
 * @author Dmitriy Moroz
 */
internal class LocalGenerationBenchmarkGateImpl(
    private val preferenceManager: PreferenceManager,
    private val repository: BenchmarkRepository,
) : LocalGenerationBenchmarkGate {

    override suspend fun evaluate(request: LocalGenerationRequest): LocalGenerationGateResult {
        if (!request.source.isLocalGenerationSource()) return LocalGenerationGateResult.Ready
        val latest = repository.getLatest()
        if (!preferenceManager.localGenerationBenchmarkPromptAnswered && latest == null) {
            return LocalGenerationGateResult.AskRunBenchmark
        } else if (!preferenceManager.localGenerationBenchmarkPromptAnswered) {
            preferenceManager.localGenerationBenchmarkPromptAnswered = true
        }
        if (preferenceManager.benchmarkRecommendationWarningSuppressed) {
            return LocalGenerationGateResult.Ready
        }
        latest ?: return LocalGenerationGateResult.Ready
        val providerRecommendation = latest.providerRecommendation(request.source)
        val recommendation = providerRecommendation?.toRecommendation() ?: latest.recommendation
        val exceeded = request.exceeded(recommendation, providerRecommendation)
        return if (exceeded.isEmpty()) {
            LocalGenerationGateResult.Ready
        } else {
            LocalGenerationGateResult.ConfirmExceedsRecommendation(
                reasons = exceeded,
                recommendation = recommendation,
            )
        }
    }

    override fun markFirstBenchmarkPromptAnswered() {
        preferenceManager.localGenerationBenchmarkPromptAnswered = true
    }

    override fun suppressRecommendationWarnings() {
        preferenceManager.benchmarkRecommendationWarningSuppressed = true
    }

    private fun LocalGenerationRequest.exceeded(
        recommendation: BenchmarkRecommendation,
        providerRecommendation: BenchmarkProviderRecommendation?,
    ): List<BenchmarkLimitExceeded> = buildList {
        if (providerRecommendation != null && !providerRecommendation.recommended) {
            add(BenchmarkLimitExceeded.PROVIDER)
            return@buildList
        }
        if (width * height > recommendation.width * recommendation.height) {
            add(BenchmarkLimitExceeded.IMAGE_SIZE)
        }
        if (samplingSteps > recommendation.samplingSteps) {
            add(BenchmarkLimitExceeded.SAMPLING_STEPS)
        }
        if (batchCount > recommendation.batchCount) {
            add(BenchmarkLimitExceeded.BATCH_COUNT)
        }
        if (hiresFix) {
            add(BenchmarkLimitExceeded.HIRES_FIX)
        }
        if (source !in recommendation.providers && recommendation.providers.isNotEmpty()) {
            add(BenchmarkLimitExceeded.PROVIDER)
        }
        if (
            source == ServerSource.LOCAL_STABLE_DIFFUSION_CPP &&
            sdxlBackend != recommendation.sdxlBackend &&
            sdxlBackend != SdxlBackend.AUTO
        ) {
            add(BenchmarkLimitExceeded.BACKEND)
        }
    }

    private fun BenchmarkProviderRecommendation.toRecommendation(): BenchmarkRecommendation =
        BenchmarkRecommendation(
            width = width.takeIf { it > 0 } ?: 256,
            height = height.takeIf { it > 0 } ?: 256,
            samplingSteps = samplingSteps.takeIf { it > 0 } ?: 8,
            cfgScale = cfgScale.takeIf { it > 0f } ?: 6f,
            batchCount = batchCount.takeIf { it > 0 } ?: 1,
            providers = if (recommended) listOf(provider) else emptyList(),
            backgroundGeneration = backgroundGeneration,
            sdxlBackend = sdxlBackend,
        )
}
