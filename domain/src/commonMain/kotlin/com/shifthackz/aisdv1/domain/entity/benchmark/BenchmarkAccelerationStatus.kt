package com.shifthackz.aisdv1.domain.entity.benchmark


/**
 * Runtime support status for an acceleration path used by local generation.
 *
 * API discovery alone is not enough: Android can report Vulkan/OpenCL/NNAPI
 * while stable-diffusion.cpp or MediaPipe cannot actually create a working
 * backend on that device.
 *
 * @author Dmitriy Moroz
 */
enum class BenchmarkAccelerationStatus {
    SUPPORTED,
    BACKEND_UNAVAILABLE,
    NOT_VALIDATED,
    NOT_RECOMMENDED,
    UNAVAILABLE,
}
