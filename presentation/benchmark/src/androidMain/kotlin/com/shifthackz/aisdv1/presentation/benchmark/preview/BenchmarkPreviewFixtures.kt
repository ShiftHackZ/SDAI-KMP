package com.shifthackz.aisdv1.presentation.benchmark.preview

import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkAccelerator
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkProviderIssue
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkProviderRecommendation
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkRecommendation
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import com.shifthackz.aisdv1.presentation.benchmark.BenchmarkState

/**
 * Fixed hardware and score samples for visual layout, not measurements or runtime recommendations.
 * No platform probe, inference engine, saved result or benchmark use case is accessed.
 *
 * @author Dmitriy Moroz
 */
internal object BenchmarkPreviewFixtures {
    val android = BenchmarkDeviceInfo(
        platform = BenchmarkPlatform.ANDROID,
        manufacturer = "Sample",
        model = "Android device",
        osVersion = "Android 16 (API 36)",
        cpuName = "Sample eight-core processor",
        cpuCores = 8,
        gpuName = "Sample mobile GPU",
        totalRamMb = 8192,
        availableRamMb = 6144,
        totalVramMb = null,
        availableVramMb = null,
        accelerators = listOf(
            BenchmarkAccelerator.VULKAN,
            BenchmarkAccelerator.OPEN_CL,
            BenchmarkAccelerator.NNAPI,
            BenchmarkAccelerator.BONSAI_VULKAN,
        ),
    )
    val ios = android.copy(
        platform = BenchmarkPlatform.IOS,
        manufacturer = "Apple",
        model = "Sample iPhone",
        osVersion = "iOS 26",
        cpuName = "Sample Apple silicon",
        gpuName = "Apple GPU",
        accelerators = listOf(
            BenchmarkAccelerator.METAL,
            BenchmarkAccelerator.CORE_ML,
            BenchmarkAccelerator.NEURAL_ENGINE,
        ),
    )
    val lowMemory = android.copy(
        manufacturer = "Google", model = "Pixel 3a",
        totalRamMb = 4096, availableRamMb = 2048,
    )
    val provider = BenchmarkProviderRecommendation(
        provider = ServerSource.LOCAL_MNN,
        recommended = true,
        width = 512,
        height = 512,
        samplingSteps = 30,
        cfgScale = 7f,
        batchCount = 1,
        estimatedTimeSeconds = 90,
        backgroundGeneration = true,
        sdxlBackend = SdxlBackend.CPU,
    )
    val result = BenchmarkResult(
        id = 1,
        createdAt = 1_700_000_000_000L,
        deviceInfo = android,
        cpuScore = 420,
        memoryScore = 330,
        acceleratorScore = 225,
        totalScore = 975,
        estimatedTimeSeconds = 90,
        recommendation = BenchmarkRecommendation(
            width = 512, height = 512, samplingSteps = 30, cfgScale = 7f, batchCount = 1,
            providers = listOf(ServerSource.LOCAL_MNN, ServerSource.LOCAL_STABLE_DIFFUSION_CPP),
            backgroundGeneration = true, sdxlBackend = SdxlBackend.CPU,
        ),
        providerRecommendations = listOf(
            provider,
            provider.copy(
                provider = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                sdxlBackend = SdxlBackend.VULKAN
            ),
            provider.copy(
                provider = ServerSource.LOCAL_MICROSOFT_ONNX, recommended = false,
                issues = listOf(BenchmarkProviderIssue.LOW_MEMORY),
            ),
        ),
    )
    val ready = BenchmarkState(
        loadingDevice = false, loadingResult = false, deviceInfo = android, latestResult = result,
    )
}
