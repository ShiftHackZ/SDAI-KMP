package com.shifthackz.aisdv1.domain.entity

/**
 * The caller's configured setup/generation deadline expired. Accepted tasks are stopped by bounded
 * task-specific cleanup; when submission was not acknowledged, the remote outcome remains unknown.
 * @param cause Original timeout retained so callers can distinguish deadline expiry from cancellation.
 * @author Dmitriy Moroz
 */
class EasyDiffusionTimeoutFailure(cause: Throwable) : Exception("Easy Diffusion timed out", cause)
