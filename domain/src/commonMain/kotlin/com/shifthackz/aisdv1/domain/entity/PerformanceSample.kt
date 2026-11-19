package com.shifthackz.aisdv1.domain.entity

/**
 * One point in the visible generation resource history. Null values mark unavailable readings,
 * leaving gaps in graphs rather than presenting failures as zero usage.
 *
 * @param cpuPercent App CPU utilization from 0 to 100, normalized across logical processors.
 * @param residentBytes App resident bytes; Android apportions shared pages using PSS, iOS reports RSS.
 * @param deviceCpuPercent Device-wide CPU usage from 0 to 100; null when restricted or unavailable.
 * @param deviceUsedMemoryBytes RAM in use across the device, including the OS and other apps.
 * @param totalMemoryBytes Device physical memory in bytes, not the app's allocation limit.
 * @author Dmitriy Moroz
 */
data class PerformanceSample(
    val cpuPercent: Float? = null,
    val residentBytes: Long? = null,
    val totalMemoryBytes: Long? = null,
    val deviceCpuPercent: Float? = null,
    val deviceUsedMemoryBytes: Long? = null,
)
