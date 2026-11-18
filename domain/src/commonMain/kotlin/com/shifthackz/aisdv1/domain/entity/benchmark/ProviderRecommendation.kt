package com.shifthackz.aisdv1.domain.entity.benchmark

import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Returns the recommendation for a concrete local provider.
 *
 * @param source local provider selected by the user.
 * @return provider recommendation or null when the benchmark cannot assess it.
 * @author Dmitriy Moroz
 */
fun BenchmarkResult.providerRecommendation(
    source: ServerSource,
): BenchmarkProviderRecommendation? =
    providerRecommendations.firstOrNull { it.provider == source }
