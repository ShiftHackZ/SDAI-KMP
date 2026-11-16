package com.shifthackz.aisdv1.domain.entity

/**
 * One complete stream object; only a succeeded terminal frame may supply a gallery image.
 * @param status Terminal result status; null denotes a progress object.
 * @param step Current sampling step, or zero while queued/loading.
 * @param totalSteps Sampling total; nonpositive totals produce indeterminate progress.
 * @param image Original encoded terminal image bytes, optionally data-URL prefixed.
 * @param seed Actual server-reported output seed; absent on progress and failed frames.
 * @param detail Remote failure detail marker; never presented as a successful result.
 * @param model Complete checkpoint identifier including any folder separators.
 * @author Dmitriy Moroz
 */
data class EasyDiffusionFrame(
    val status: String? = null,
    val step: Int = 0,
    val totalSteps: Int = 0,
    val image: String? = null,
    val seed: Long? = null,
    val detail: String? = null,
    val model: String? = null,
)
