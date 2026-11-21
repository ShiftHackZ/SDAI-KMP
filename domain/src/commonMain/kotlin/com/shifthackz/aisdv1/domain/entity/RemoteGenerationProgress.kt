package com.shifthackz.aisdv1.domain.entity

/**
 * Measured progress for one remote submission, retained only while its caller is active.
 *
 * @param source Provider that owns this submission.
 * @param ownerId WorkManager UUID, or null for a foreground screen.
 * @param percentage Job/workflow completion, or null while queued or unavailable.
 * @author Dmitriy Moroz
 */
data class RemoteGenerationProgress(
    val source: ServerSource,
    val ownerId: String?,
    val percentage: Int?
)
