package com.shifthackz.aisdv1.domain.entity.benchmark


/**
 * Machine-readable reasons attached to provider recommendations.
 *
 * @author Dmitriy Moroz
 */
enum class BenchmarkProviderIssue {
    PLATFORM_UNSUPPORTED,
    LOW_MEMORY,
    LOW_SCORE,
    ONNX_SLOW_LOW_MEMORY,
    MEDIAPIPE_UNSTABLE_LOW_MEMORY,
    SDXL_TINY_EXPERIMENTAL_ONLY,
    SDXL_BACKEND_NOT_VALIDATED,
    ACCELERATOR_API_NOT_AVAILABLE,
}
