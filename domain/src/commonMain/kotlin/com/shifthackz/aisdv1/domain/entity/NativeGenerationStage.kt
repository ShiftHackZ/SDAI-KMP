package com.shifthackz.aisdv1.domain.entity

/**
 * Sanitized native execution stages, independent of model paths or prompt contents.
 *
 * @author Dmitriy Moroz
 */
enum class NativeGenerationStage {
    PREPARING,
    TOKENIZING,
    DENOISING,
    DECODING,

    /**
     * Opaque native work spanning model load, conditioning, sampling and decode.
     * Its counters cannot be interpreted as sampling steps. Ordered before result publication
     * for monotonic journals; protocol and journal storage use stage names rather than ordinals.
     *
     * @author Dmitriy Moroz
     */
    RUNNING,
    WRITING_RESULT,
}
