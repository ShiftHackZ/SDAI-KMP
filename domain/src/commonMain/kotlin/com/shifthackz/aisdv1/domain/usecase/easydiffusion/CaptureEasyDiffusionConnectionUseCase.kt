package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionConnection

/** Captures an internally consistent connection for generation or explicit model refresh.
 * @author Dmitriy Moroz */
interface CaptureEasyDiffusionConnectionUseCase {
    /** Rejects a replaced selection before returning its endpoint, checkpoint and original revision.
     * @param expectedRevision Submitted revision; null supports legacy queued work using current settings.
     * @param expectedUrl Optional endpoint whose catalog is being refreshed; a changed URL rejects the refresh.
     * @return Immutable connection captured while configuration replacement is excluded.
     * @author Dmitriy Moroz */
    suspend operator fun invoke(
        expectedRevision: Long?,
        expectedUrl: String? = null
    ): EasyDiffusionConnection
}
