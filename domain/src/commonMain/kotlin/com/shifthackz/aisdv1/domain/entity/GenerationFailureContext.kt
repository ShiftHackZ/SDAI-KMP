package com.shifthackz.aisdv1.domain.entity

/**
 * Token-free historical exit evidence; absence never proves a particular native failure.
 *
 * @param timestamp Exit wall-clock timestamp.
 * @param reason Android exit reason code, preserved without guessing its cause.
 * @param status OS exit status/signal.
 * @param hasNativeTrace Whether API 31+ supplied a native/ANR trace stream.
 * @author Dmitriy Moroz
 */
data class GenerationFailureContext(
    val timestamp: Long,
    val reason: Int,
    val status: Int,
    val hasNativeTrace: Boolean
)
