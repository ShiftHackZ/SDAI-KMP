package com.shifthackz.aisdv1.domain.entity

/**
 * Raw application resource counters, sampled together off the UI thread.
 *
 * @param elapsedNanos Monotonic time used to measure the sampling interval.
 * @param cpuTimeNanos Cumulative user and kernel CPU time by process lifetime identity.
 * @param processorCount Logical processors used to normalize CPU utilization to device capacity.
 * @param residentBytes Resident app memory, including Android inference processes.
 * @param deviceCpu Device-wide tick counters, or the actual platform access/read failure.
 * @param deviceUsedMemoryBytes Device RAM in use, including the OS and other applications.
 * @param totalMemoryBytes Physical memory available to the operating system.
 * @author Dmitriy Moroz
 */
data class PerformanceReading(
    val elapsedNanos: Long,
    val cpuTimeNanos: Map<String, Long>,
    val processorCount: Int,
    val residentBytes: Long,
    val totalMemoryBytes: Long,
    val deviceCpu: Result<CpuUsageCounters> = Result.failure(UnsupportedOperationException("Device CPU counters unavailable")),
    val deviceUsedMemoryBytes: Long? = null,
)
