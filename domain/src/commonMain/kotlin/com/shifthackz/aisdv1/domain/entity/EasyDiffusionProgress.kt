package com.shifthackz.aisdv1.domain.entity

/**
 * Progress of the current remote operation; nonpositive totals mean queued or loading.
 * @param ownerId Background job UUID associated with this operation; null for foreground generation.
 * @param step Current sampling step, or zero while queued/loading.
 * @param totalSteps Sampling total; nonpositive totals produce indeterminate progress.
 * @param isRunning Whether the shared operation is currently queued or running.
 * @author Dmitriy Moroz
 */
data class EasyDiffusionProgress(
    val step: Int = 0,
    val totalSteps: Int = 0,
    val isRunning: Boolean = false,
    val ownerId: String? = null
)
