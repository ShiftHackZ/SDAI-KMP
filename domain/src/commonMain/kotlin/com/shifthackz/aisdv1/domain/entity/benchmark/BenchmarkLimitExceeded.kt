package com.shifthackz.aisdv1.domain.entity.benchmark


/**
 * Reasons why a local request exceeds benchmark recommendations.
 *
 * @author Dmitriy Moroz
 */
enum class BenchmarkLimitExceeded {
    IMAGE_SIZE,
    SAMPLING_STEPS,
    BATCH_COUNT,
    HIRES_FIX,
    PROVIDER,
    BACKEND,
}
