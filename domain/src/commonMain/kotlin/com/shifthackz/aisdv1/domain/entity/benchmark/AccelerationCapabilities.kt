package com.shifthackz.aisdv1.domain.entity.benchmark

import com.shifthackz.aisdv1.domain.policy.benchmark.BenchmarkRecommendationPolicy

/**
 * Returns acceleration capabilities as runtime-backend support, not raw OS API
 * presence.
 *
 * @author Dmitriy Moroz
 */
fun BenchmarkDeviceInfo.accelerationCapabilities(): List<BenchmarkAccelerationCapability> =
    BenchmarkAccelerator.values().map { accelerator ->
        BenchmarkAccelerationCapability(
            accelerator = accelerator,
            apiDetected = accelerator in accelerators,
            status = accelerationStatus(accelerator),
        )
    }

private fun BenchmarkDeviceInfo.accelerationStatus(
    accelerator: BenchmarkAccelerator,
): BenchmarkAccelerationStatus {
    val apiDetected = accelerator in accelerators
    return when (platform) {
        BenchmarkPlatform.ANDROID -> when (accelerator) {
            BenchmarkAccelerator.BONSAI_VULKAN -> when {
                !apiDetected -> BenchmarkAccelerationStatus.UNAVAILABLE
                else -> BenchmarkAccelerationStatus.SUPPORTED
            }

            BenchmarkAccelerator.VULKAN,
            BenchmarkAccelerator.OPEN_CL -> when {
                !apiDetected -> BenchmarkAccelerationStatus.UNAVAILABLE
                BenchmarkRecommendationPolicy.isPixel3aClass(this) ->
                    BenchmarkAccelerationStatus.BACKEND_UNAVAILABLE

                else -> BenchmarkAccelerationStatus.NOT_VALIDATED
            }

            BenchmarkAccelerator.NNAPI -> when {
                !apiDetected -> BenchmarkAccelerationStatus.UNAVAILABLE
                BenchmarkRecommendationPolicy.isPixel3aClass(this) ||
                        BenchmarkRecommendationPolicy.effectiveRamMb(this) < 4_500L ->
                    BenchmarkAccelerationStatus.NOT_RECOMMENDED

                else -> BenchmarkAccelerationStatus.NOT_VALIDATED
            }

            BenchmarkAccelerator.METAL,
            BenchmarkAccelerator.CORE_ML,
            BenchmarkAccelerator.NEURAL_ENGINE -> BenchmarkAccelerationStatus.UNAVAILABLE
        }

        BenchmarkPlatform.IOS -> when (accelerator) {
            BenchmarkAccelerator.METAL,
            BenchmarkAccelerator.CORE_ML,
            BenchmarkAccelerator.NEURAL_ENGINE ->
                if (apiDetected) {
                    BenchmarkAccelerationStatus.SUPPORTED
                } else {
                    BenchmarkAccelerationStatus.UNAVAILABLE
                }

            BenchmarkAccelerator.VULKAN,
            BenchmarkAccelerator.BONSAI_VULKAN,
            BenchmarkAccelerator.OPEN_CL,
            BenchmarkAccelerator.NNAPI -> BenchmarkAccelerationStatus.UNAVAILABLE
        }

        BenchmarkPlatform.UNKNOWN -> BenchmarkAccelerationStatus.UNAVAILABLE
    }
}
