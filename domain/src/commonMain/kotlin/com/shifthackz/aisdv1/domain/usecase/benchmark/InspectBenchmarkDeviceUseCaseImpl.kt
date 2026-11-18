package com.shifthackz.aisdv1.domain.usecase.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.gateway.BenchmarkDeviceProbe

/**
 * Captures a static platform hardware snapshot without loading an inference runtime.
 *
 * @param probe Reads OS/device capabilities for display and conservative recommendation policy.
 *
 * @author Dmitriy Moroz
 */
internal class InspectBenchmarkDeviceUseCaseImpl(private val probe: BenchmarkDeviceProbe) :
    InspectBenchmarkDeviceUseCase {
    override suspend operator fun invoke(): BenchmarkDeviceInfo = probe.capture()
}
