package com.shifthackz.aisdv1.domain.entity

/**
 * Resource counters read inside an inference process rather than through restricted cross-process files.
 * @param identity Process incarnation used to avoid joining CPU counters across worker restarts.
 * @param cpuTimeNanos Cumulative user and kernel CPU execution time.
 * @param residentBytes Resident process memory, including native allocations and model mappings.
 * @author Dmitriy Moroz
 */
data class ProcessPerformanceReading(
    val identity: String,
    val cpuTimeNanos: Long,
    val residentBytes: Long,
)
