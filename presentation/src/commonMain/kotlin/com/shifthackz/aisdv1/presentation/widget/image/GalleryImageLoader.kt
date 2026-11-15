package com.shifthackz.aisdv1.presentation.widget.image

import androidx.compose.ui.graphics.ImageBitmap
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticContext

/**
 * Bounded image display cache keyed by persisted content identity and target size.
 *
 * @author Dmitriy Moroz
 */
interface GalleryImageLoader {
    /**
     * Returns a cached or bounded sampled display image for the requested content revision.
     * Cache hits do not recheck storage; callers must pass the current revision. On a cache miss,
     * missing, replaced, over-limit or undecodable source content returns null. Cancellation propagates.
     *
     * @param id Persisted gallery identity.
     * @param revision Expected content revision.
     * @param isInput Input/output image selection.
     * @param maxDimension Requested display edge.
     * @param diagnosticContext Optional fixed-label caller context; never retained by the cache.
     * @author Dmitriy Moroz
     */
    suspend fun load(
        id: Long,
        revision: String,
        isInput: Boolean,
        maxDimension: Int,
        diagnosticContext: UiDiagnosticContext? = null
    ): ImageBitmap?

    /**
     * Samples an unsaved result under the same byte/concurrency/display limits.
     *
     * @param revision Transient identity allocated once for this result.
     * @param base64 Original retained for explicit save/share.
     * @param isInput Input/output cache distinction.
     * @param maxDimension Measured display edge.
     * @param diagnosticContext Optional fixed-label caller context; never retained by the cache.
     * @author Dmitriy Moroz
     */
    suspend fun loadTransient(
        revision: String,
        base64: String,
        isInput: Boolean,
        maxDimension: Int,
        diagnosticContext: UiDiagnosticContext? = null
    ): ImageBitmap?
}
