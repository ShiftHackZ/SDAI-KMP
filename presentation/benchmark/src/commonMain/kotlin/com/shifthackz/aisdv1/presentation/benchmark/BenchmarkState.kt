package com.shifthackz.aisdv1.presentation.benchmark

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult

/**
 * Holds benchmark screen state.
 *
 * @param loadingDevice true while the screen captures hardware metadata.
 * @param loadingResult true while the latest stored benchmark result is being loaded.
 * @param running true while a synthetic benchmark is executing.
 * @param deviceInfo latest best-effort hardware snapshot.
 * @param latestResult most recent stored benchmark result.
 * @param error current user-facing failure message.
 * @author Dmitriy Moroz
 */
data class BenchmarkState(
    val loadingDevice: Boolean = true,
    val loadingResult: Boolean = true,
    val running: Boolean = false,
    val deviceInfo: BenchmarkDeviceInfo? = null,
    val latestResult: BenchmarkResult? = null,
    val error: String? = null,
) : MviState
