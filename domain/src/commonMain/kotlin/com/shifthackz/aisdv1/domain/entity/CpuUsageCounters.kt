package com.shifthackz.aisdv1.domain.entity

/**
 * Cumulative device-wide CPU counters in the platform's native tick unit.
 * @param activeTicks Time spent executing across all logical processors.
 * @param totalTicks Execution and idle time across those same processors.
 * @author Dmitriy Moroz
 */
data class CpuUsageCounters(val activeTicks: Long, val totalTicks: Long)
