package com.shifthackz.aisdv1.domain.entity.benchmark


/**
 * Persisted benchmark score with the device snapshot and recommendations.
 *
 * @param id Persistent benchmark identity, assigned when stored.
 * @param createdAt Wall-clock timestamp when the result was measured.
 * @param deviceInfo Device snapshot measured with this result.
 * @param cpuScore Cpu score.
 * @param memoryScore Memory score.
 * @param acceleratorScore Accelerator score.
 * @param totalScore Total score.
 * @param estimatedTimeSeconds Estimated warm generation duration in seconds.
 * @param recommendation Saved limits used for the admission decision.
 * @param providerRecommendations Per-provider recommendations derived from the same measurement.
 * @param notes Diagnostics explaining conservative recommendations.
 * @author Dmitriy Moroz
 */
data class BenchmarkResult(
    val id: Long = 0L,
    val createdAt: Long,
    val deviceInfo: BenchmarkDeviceInfo,
    val cpuScore: Int,
    val memoryScore: Int,
    val acceleratorScore: Int,
    val totalScore: Int,
    val estimatedTimeSeconds: Int,
    val recommendation: BenchmarkRecommendation,
    val providerRecommendations: List<BenchmarkProviderRecommendation> = emptyList(),
    val notes: List<String> = emptyList(),
)
