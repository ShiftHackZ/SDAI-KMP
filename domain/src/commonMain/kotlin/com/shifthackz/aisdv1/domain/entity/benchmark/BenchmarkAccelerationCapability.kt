package com.shifthackz.aisdv1.domain.entity.benchmark


/**
 * Capability row rendered by the benchmark screen.
 *
 * @param accelerator Acceleration technology assessed by this row.
 * @param apiDetected Whether the OS advertises this acceleration API.
 * @param status Runtime support classification, distinct from raw API detection.
 * @author Dmitriy Moroz
 */
data class BenchmarkAccelerationCapability(
    val accelerator: BenchmarkAccelerator,
    val apiDetected: Boolean,
    val status: BenchmarkAccelerationStatus,
)
