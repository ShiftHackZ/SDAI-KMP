package com.shifthackz.aisdv1.app.inference

/**
 * Host ownership guard against late cancellation from a replaced WorkManager request.
 *
 * @param connection Unique binding for this attempt.
 * @param ownerId Work UUID, or null for foreground generation.
 * @author Dmitriy Moroz
 */
internal data class OwnedInferenceConnection(
    val connection: InferenceConnection,
    val ownerId: String?
)
