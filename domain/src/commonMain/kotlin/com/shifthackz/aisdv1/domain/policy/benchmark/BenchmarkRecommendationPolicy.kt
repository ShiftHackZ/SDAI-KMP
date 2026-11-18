package com.shifthackz.aisdv1.domain.policy.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkProviderRecommendation
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkRecommendation
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkAccelerator
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkAccelerationStatus
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkProviderIssue
import com.shifthackz.aisdv1.domain.entity.benchmark.accelerationCapabilities
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import kotlin.math.roundToInt

/**
 * Derives conservative local-provider settings from the captured device and synthetic scores.
 * This pure policy never probes hardware, reads storage or initializes a runtime.
 * @author Dmitriy Moroz
 */
object BenchmarkRecommendationPolicy {

    /**
     * Ranks supported local providers and derives safe initial generation limits.
     * @param deviceInfo Captured hardware snapshot; no new device access occurs.
     * @param totalScore Combined synthetic benchmark score.
     * @param cpuScore Synthetic CPU score.
     * @param memoryScore Synthetic memory-workload score.
     * @param acceleratorScore Capability-derived acceleration score.
     * @author Dmitriy Moroz
     */
    fun providerRecommendations(
        deviceInfo: BenchmarkDeviceInfo,
        totalScore: Int,
        cpuScore: Int,
        memoryScore: Int,
        acceleratorScore: Int,
    ): List<BenchmarkProviderRecommendation> = when (deviceInfo.platform) {
        BenchmarkPlatform.ANDROID -> androidRecommendations(
            deviceInfo = deviceInfo,
            totalScore = totalScore,
            cpuScore = cpuScore,
            memoryScore = memoryScore,
            acceleratorScore = acceleratorScore,
        )

        BenchmarkPlatform.IOS -> iosRecommendations(deviceInfo, totalScore)
        BenchmarkPlatform.UNKNOWN -> emptyList()
    }

    /**
     * Selects the first usable provider recommendation, retaining conservative defaults if none qualify.
     * @param providerRecommendations Provider results in their existing preference order.
     * @author Dmitriy Moroz
     */
    fun fallbackRecommendation(
        providerRecommendations: List<BenchmarkProviderRecommendation>,
    ): BenchmarkRecommendation {
        val recommended =
            providerRecommendations.filter(BenchmarkProviderRecommendation::recommended)
        val primary = recommended.firstOrNull()
        return BenchmarkRecommendation(
            width = primary?.width?.takeIf { it > 0 } ?: 256,
            height = primary?.height?.takeIf { it > 0 } ?: 256,
            samplingSteps = primary?.samplingSteps?.takeIf { it > 0 } ?: 8,
            cfgScale = primary?.cfgScale?.takeIf { it > 0f } ?: 6f,
            batchCount = primary?.batchCount?.takeIf { it > 0 } ?: 1,
            providers = recommended.map(BenchmarkProviderRecommendation::provider),
            backgroundGeneration = primary?.backgroundGeneration ?: false,
            sdxlBackend = recommended
                .firstOrNull { it.provider == ServerSource.LOCAL_STABLE_DIFFUSION_CPP }
                ?.sdxlBackend
                ?: SdxlBackend.CPU,
        )
    }

    /**
     * Estimates warm generation duration from provider, dimensions and synthetic scores.
     * @param provider Local generation provider being assessed.
     * @param width Requested output width in pixels.
     * @param height Requested output height in pixels.
     * @param samplingSteps Requested inference step count.
     * @param totalScore Combined synthetic benchmark score.
     * @param deviceInfo Captured hardware snapshot; no new device access occurs.
     * @author Dmitriy Moroz
     */
    fun estimateGenerationSeconds(
        provider: ServerSource,
        width: Int,
        height: Int,
        samplingSteps: Int,
        totalScore: Int,
        deviceInfo: BenchmarkDeviceInfo,
    ): Int {
        when (provider) {
            ServerSource.LOCAL_APPLE_CORE_ML -> {
                return estimateCoreMlWarmGenerationSeconds(
                    width = width,
                    height = height,
                    samplingSteps = samplingSteps,
                    totalScore = totalScore,
                    deviceInfo = deviceInfo,
                )
            }

            ServerSource.LOCAL_APPLE_BONSAI -> {
                return estimateBonsaiWarmGenerationSeconds(
                    width = width,
                    height = height,
                    samplingSteps = samplingSteps,
                    totalScore = totalScore,
                    deviceInfo = deviceInfo,
                )
            }

            else -> Unit
        }
        val pixelFactor = width * height / (512f * 512f)
        val stepFactor = samplingSteps / 20f
        val scoreFactor = 2_400f / totalScore.coerceAtLeast(450)
        val providerFactor = when (provider) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> if (isPixel3aClass(deviceInfo)) 3.8f else 1.7f
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> 1.15f
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> if (isPixel3aClass(deviceInfo)) 6.0f else 2.8f
            ServerSource.LOCAL_APPLE_CORE_ML -> 0.62f
            else -> 1.0f
        }
        return (72f * pixelFactor * stepFactor * scoreFactor * providerFactor)
            .roundToInt()
            .coerceIn(8, 900)
    }

    /**
     * Uses installed RAM when reported, otherwise the available-memory snapshot.
     * @param deviceInfo Captured hardware snapshot; no new device access occurs.
     * @author Dmitriy Moroz
     */
    fun effectiveRamMb(deviceInfo: BenchmarkDeviceInfo): Long =
        when {
            deviceInfo.platform == BenchmarkPlatform.IOS && deviceInfo.totalRamMb > 24_000L -> 8_192L
            else -> deviceInfo.totalRamMb
        }.coerceAtLeast(512L)

    /**
     * Identifies the low-memory Pixel 3a device family used by conservative Android limits.
     * @param deviceInfo Captured hardware snapshot; no new device access occurs.
     * @author Dmitriy Moroz
     */
    fun isPixel3aClass(deviceInfo: BenchmarkDeviceInfo): Boolean {
        val value = listOf(
            deviceInfo.manufacturer,
            deviceInfo.model,
            deviceInfo.cpuName,
            deviceInfo.gpuName,
        ).joinToString(" ").lowercase()
        return "pixel 3a" in value ||
                "sargo" in value ||
                "bonito" in value ||
                "sdm670" in value ||
                "adreno 615" in value
    }

    private fun androidRecommendations(
        deviceInfo: BenchmarkDeviceInfo,
        totalScore: Int,
        cpuScore: Int,
        memoryScore: Int,
        acceleratorScore: Int,
    ): List<BenchmarkProviderRecommendation> {
        val ramMb = effectiveRamMb(deviceInfo)
        val lowMemory = ramMb < 4_000L
        val lowScore = totalScore < 1_450
        val pixel3aClass = isPixel3aClass(deviceInfo)
        val onnx = onnxRecommendation(deviceInfo, totalScore, lowMemory, lowScore, pixel3aClass)
        val mediaPipe = mediaPipeRecommendation(deviceInfo, totalScore, lowMemory, pixel3aClass)
        val sdxl = sdxlRecommendation(
            deviceInfo = deviceInfo,
            totalScore = totalScore,
            cpuScore = cpuScore,
            memoryScore = memoryScore,
            acceleratorScore = acceleratorScore,
            lowMemory = lowMemory,
            pixel3aClass = pixel3aClass,
        )
        return listOf(onnx, mediaPipe, sdxl)
    }

    private fun iosRecommendations(
        deviceInfo: BenchmarkDeviceInfo,
        totalScore: Int,
    ): List<BenchmarkProviderRecommendation> {
        val ramMb = effectiveRamMb(deviceInfo)
        val hasCoreMl = BenchmarkAccelerator.CORE_ML in deviceInfo.accelerators
        val hasMetal = BenchmarkAccelerator.METAL in deviceInfo.accelerators
        return buildList {
            if (hasCoreMl) {
                val profile = iosCoreMlProfile(
                    deviceInfo = deviceInfo,
                    totalScore = totalScore,
                    ramMb = ramMb,
                )
                add(
                    recommended(
                        provider = ServerSource.LOCAL_APPLE_CORE_ML,
                        width = profile.size,
                        height = profile.size,
                        samplingSteps = profile.samplingSteps,
                        cfgScale = 7f,
                        estimatedTimeSeconds = profile.estimatedTimeSeconds,
                        backgroundGeneration = profile.backgroundGeneration,
                    ),
                )
            } else {
                add(
                    notRecommended(
                        provider = ServerSource.LOCAL_APPLE_CORE_ML,
                        issues = listOf(BenchmarkProviderIssue.ACCELERATOR_API_NOT_AVAILABLE),
                    ),
                )
            }

            if (hasMetal) {
                val profile = iosBonsaiProfile(
                    deviceInfo = deviceInfo,
                    totalScore = totalScore,
                    ramMb = ramMb,
                )
                add(
                    recommended(
                        provider = ServerSource.LOCAL_APPLE_BONSAI,
                        width = profile.size,
                        height = profile.size,
                        samplingSteps = profile.samplingSteps,
                        cfgScale = 7f,
                        estimatedTimeSeconds = profile.estimatedTimeSeconds,
                        backgroundGeneration = profile.backgroundGeneration,
                    ),
                )
            } else {
                add(
                    notRecommended(
                        provider = ServerSource.LOCAL_APPLE_BONSAI,
                        issues = listOf(BenchmarkProviderIssue.ACCELERATOR_API_NOT_AVAILABLE),
                    ),
                )
            }
        }
    }

    private fun iosCoreMlProfile(
        deviceInfo: BenchmarkDeviceInfo,
        totalScore: Int,
        ramMb: Long,
    ): IosLocalGenerationProfile {
        val (size, steps, backgroundGeneration) = when {
            deviceInfo.isHighEndIosCoreMlDevice(ramMb) -> Triple(512, 50, true)
            ramMb >= 6_000L && totalScore >= 3_000 -> Triple(512, 30, true)
            ramMb >= 4_000L -> Triple(384, 20, false)
            else -> Triple(384, 12, false)
        }
        return IosLocalGenerationProfile(
            size = size,
            samplingSteps = steps,
            estimatedTimeSeconds = estimateCoreMlWarmGenerationSeconds(
                width = size,
                height = size,
                samplingSteps = steps,
                totalScore = totalScore,
                deviceInfo = deviceInfo,
            ),
            backgroundGeneration = backgroundGeneration,
        )
    }

    private fun iosBonsaiProfile(
        deviceInfo: BenchmarkDeviceInfo,
        totalScore: Int,
        ramMb: Long,
    ): IosLocalGenerationProfile {
        val (size, steps, backgroundGeneration) = when {
            deviceInfo.isHighEndIosCoreMlDevice(ramMb) -> Triple(512, 20, true)
            ramMb >= 6_000L && totalScore >= 3_000 -> Triple(512, 16, true)
            ramMb >= 4_000L -> Triple(384, 12, false)
            else -> Triple(384, 8, false)
        }
        return IosLocalGenerationProfile(
            size = size,
            samplingSteps = steps,
            estimatedTimeSeconds = estimateBonsaiWarmGenerationSeconds(
                width = size,
                height = size,
                samplingSteps = steps,
                totalScore = totalScore,
                deviceInfo = deviceInfo,
            ),
            backgroundGeneration = backgroundGeneration,
        )
    }

    private fun estimateCoreMlWarmGenerationSeconds(
        width: Int,
        height: Int,
        samplingSteps: Int,
        totalScore: Int,
        deviceInfo: BenchmarkDeviceInfo,
    ): Int {
        val ramMb = effectiveRamMb(deviceInfo)
        val pixelFactor = width * height / (512f * 512f)
        val millisPerStepAt512 = when {
            deviceInfo.isHighEndIosCoreMlDevice(ramMb) -> 900f
            totalScore >= 4_200 && ramMb >= 6_000L -> 1_300f
            totalScore >= 3_000 && ramMb >= 5_000L -> 1_600f
            else -> 2_100f
        }
        return (samplingSteps * millisPerStepAt512 * pixelFactor / 1_000f)
            .roundToInt()
            .coerceIn(3, 900)
    }

    private fun estimateBonsaiWarmGenerationSeconds(
        width: Int,
        height: Int,
        samplingSteps: Int,
        totalScore: Int,
        deviceInfo: BenchmarkDeviceInfo,
    ): Int {
        val ramMb = effectiveRamMb(deviceInfo)
        val pixelFactor = width * height / (512f * 512f)
        val millisPerStepAt512 = when {
            deviceInfo.isHighEndIosCoreMlDevice(ramMb) -> 870f
            totalScore >= 4_200 && ramMb >= 6_000L -> 1_050f
            totalScore >= 3_000 && ramMb >= 5_000L -> 1_350f
            else -> 1_900f
        }
        return (samplingSteps * millisPerStepAt512 * pixelFactor / 1_000f)
            .roundToInt()
            .coerceIn(3, 900)
    }

    private fun BenchmarkDeviceInfo.isHighEndIosCoreMlDevice(ramMb: Long): Boolean {
        if (platform != BenchmarkPlatform.IOS || ramMb < 8_000L) return false
        if (BenchmarkAccelerator.METAL !in accelerators ||
            BenchmarkAccelerator.CORE_ML !in accelerators ||
            BenchmarkAccelerator.NEURAL_ENGINE !in accelerators
        ) return false
        val value = listOf(manufacturer, model, cpuName, gpuName)
            .joinToString(" ")
            .lowercase()
        return ramMb >= 10_000L ||
                "a17 pro" in value ||
                "a18" in value ||
                "a19" in value ||
                "iphone 15 pro" in value ||
                "iphone 16 pro" in value ||
                "iphone 17 pro" in value ||
                "m1" in value ||
                "m2" in value ||
                "m3" in value ||
                "m4" in value ||
                "m5" in value
    }

    private fun onnxRecommendation(
        deviceInfo: BenchmarkDeviceInfo,
        totalScore: Int,
        lowMemory: Boolean,
        lowScore: Boolean,
        pixel3aClass: Boolean,
    ): BenchmarkProviderRecommendation {
        val issues = buildList {
            if (lowMemory || pixel3aClass) add(BenchmarkProviderIssue.ONNX_SLOW_LOW_MEMORY)
            if (lowScore && !lowMemory && !pixel3aClass) add(BenchmarkProviderIssue.LOW_SCORE)
        }
        val (size, steps, estimate) = when {
            pixel3aClass || lowMemory || lowScore -> Triple(256, 8, 270)
            totalScore < 2_400 -> Triple(
                384, 12, estimateGenerationSeconds(
                    provider = ServerSource.LOCAL_MICROSOFT_ONNX,
                    width = 384,
                    height = 384,
                    samplingSteps = 12,
                    totalScore = totalScore,
                    deviceInfo = deviceInfo,
                )
            )

            else -> Triple(
                512, 16, estimateGenerationSeconds(
                    provider = ServerSource.LOCAL_MICROSOFT_ONNX,
                    width = 512,
                    height = 512,
                    samplingSteps = 16,
                    totalScore = totalScore,
                    deviceInfo = deviceInfo,
                )
            )
        }
        return recommended(
            provider = ServerSource.LOCAL_MICROSOFT_ONNX,
            width = size,
            height = size,
            samplingSteps = steps,
            cfgScale = 6f,
            estimatedTimeSeconds = estimate,
            backgroundGeneration = false,
            issues = issues,
        )
    }

    private fun mediaPipeRecommendation(
        deviceInfo: BenchmarkDeviceInfo,
        totalScore: Int,
        lowMemory: Boolean,
        pixel3aClass: Boolean,
    ): BenchmarkProviderRecommendation {
        val nnapiStatus = deviceInfo.accelerationStatus(BenchmarkAccelerator.NNAPI)
        val ramMb = effectiveRamMb(deviceInfo)
        if (nnapiStatus == BenchmarkAccelerationStatus.UNAVAILABLE) {
            return notRecommended(
                provider = ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                issues = listOf(BenchmarkProviderIssue.ACCELERATOR_API_NOT_AVAILABLE),
            )
        }
        if (pixel3aClass ||
            lowMemory ||
            ramMb < 4_500L ||
            nnapiStatus == BenchmarkAccelerationStatus.NOT_RECOMMENDED
        ) {
            return notRecommended(
                provider = ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                issues = listOf(BenchmarkProviderIssue.MEDIAPIPE_UNSTABLE_LOW_MEMORY),
            )
        }
        if (totalScore < 2_300) {
            return notRecommended(
                provider = ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                issues = listOf(BenchmarkProviderIssue.LOW_SCORE),
            )
        }
        val size = if (totalScore >= 4_000 && ramMb >= 6_000L) 512 else 384
        val steps = if (size == 512) 20 else 14
        return recommended(
            provider = ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
            width = size,
            height = size,
            samplingSteps = steps,
            cfgScale = 7f,
            estimatedTimeSeconds = estimateGenerationSeconds(
                provider = ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                width = size,
                height = size,
                samplingSteps = steps,
                totalScore = totalScore,
                deviceInfo = deviceInfo,
            ),
            backgroundGeneration = totalScore >= 3_200,
        )
    }

    private fun sdxlRecommendation(
        deviceInfo: BenchmarkDeviceInfo,
        totalScore: Int,
        cpuScore: Int,
        memoryScore: Int,
        acceleratorScore: Int,
        lowMemory: Boolean,
        pixel3aClass: Boolean,
    ): BenchmarkProviderRecommendation {
        val ramMb = effectiveRamMb(deviceInfo)
        if (pixel3aClass || lowMemory) {
            return notRecommended(
                provider = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                sdxlBackend = SdxlBackend.CPU,
                issues = listOf(BenchmarkProviderIssue.SDXL_TINY_EXPERIMENTAL_ONLY),
            )
        }
        if (ramMb < 6_000L || totalScore < 2_200 || cpuScore < 1_200 || memoryScore < 1_200) {
            return notRecommended(
                provider = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                sdxlBackend = SdxlBackend.CPU,
                issues = listOf(BenchmarkProviderIssue.LOW_SCORE),
            )
        }
        val backend = when {
            deviceInfo.accelerationStatus(BenchmarkAccelerator.OPEN_CL) == BenchmarkAccelerationStatus.SUPPORTED &&
                    ramMb >= 8_000L &&
                    totalScore >= 3_800 &&
                    acceleratorScore >= 900 -> SdxlBackend.OPEN_CL

            else -> SdxlBackend.CPU
        }
        val size = when {
            ramMb >= 10_000L && totalScore >= 5_200 -> 512
            ramMb >= 8_000L && totalScore >= 3_600 -> 384
            else -> 256
        }
        val steps = when (size) {
            512 -> 18
            384 -> 14
            else -> 10
        }
        val issues = buildList {
            if (BenchmarkAccelerator.VULKAN in deviceInfo.accelerators) {
                add(BenchmarkProviderIssue.SDXL_BACKEND_NOT_VALIDATED)
            }
        }
        return recommended(
            provider = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            width = size,
            height = size,
            samplingSteps = steps,
            cfgScale = 6f,
            estimatedTimeSeconds = estimateGenerationSeconds(
                provider = ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
                width = size,
                height = size,
                samplingSteps = steps,
                totalScore = totalScore,
                deviceInfo = deviceInfo,
            ),
            backgroundGeneration = totalScore >= 3_600 && ramMb >= 8_000L,
            sdxlBackend = backend,
            issues = issues,
        )
    }

    private fun recommended(
        provider: ServerSource,
        width: Int,
        height: Int,
        samplingSteps: Int,
        cfgScale: Float,
        estimatedTimeSeconds: Int,
        backgroundGeneration: Boolean,
        batchCount: Int = 1,
        sdxlBackend: SdxlBackend = SdxlBackend.CPU,
        issues: List<BenchmarkProviderIssue> = emptyList(),
    ): BenchmarkProviderRecommendation = BenchmarkProviderRecommendation(
        provider = provider,
        recommended = true,
        width = width,
        height = height,
        samplingSteps = samplingSteps,
        cfgScale = cfgScale,
        batchCount = batchCount,
        estimatedTimeSeconds = estimatedTimeSeconds,
        backgroundGeneration = backgroundGeneration,
        sdxlBackend = sdxlBackend,
        issues = issues,
    )

    private fun notRecommended(
        provider: ServerSource,
        issues: List<BenchmarkProviderIssue>,
        sdxlBackend: SdxlBackend = SdxlBackend.CPU,
    ): BenchmarkProviderRecommendation = BenchmarkProviderRecommendation(
        provider = provider,
        recommended = false,
        width = 0,
        height = 0,
        samplingSteps = 0,
        cfgScale = 0f,
        batchCount = 1,
        estimatedTimeSeconds = 0,
        backgroundGeneration = false,
        sdxlBackend = sdxlBackend,
        issues = issues,
    )

    private fun BenchmarkDeviceInfo.accelerationStatus(
        accelerator: BenchmarkAccelerator,
    ): BenchmarkAccelerationStatus =
        accelerationCapabilities()
            .firstOrNull { it.accelerator == accelerator }
            ?.status
            ?: BenchmarkAccelerationStatus.UNAVAILABLE
}

private data class IosLocalGenerationProfile(
    val size: Int,
    val samplingSteps: Int,
    val estimatedTimeSeconds: Int,
    val backgroundGeneration: Boolean,
)
