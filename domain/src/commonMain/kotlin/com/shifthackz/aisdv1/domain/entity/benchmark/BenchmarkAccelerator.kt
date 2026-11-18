package com.shifthackz.aisdv1.domain.entity.benchmark


/**
 * Hardware acceleration capabilities relevant for on-device image generation.
 *
 * @author Dmitriy Moroz
 */
enum class BenchmarkAccelerator {
    VULKAN,
    BONSAI_VULKAN,
    OPEN_CL,
    NNAPI,
    METAL,
    CORE_ML,
    NEURAL_ENGINE,
}
