package com.shifthackz.aisdv1.domain.usecase.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo

/** Captures device capabilities without loading an inference runtime.
 * @author Dmitriy Moroz
 */
interface InspectBenchmarkDeviceUseCase {
    /**
     * Returns current OS/device capabilities; capture failures and cancellation propagate.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): BenchmarkDeviceInfo
}
