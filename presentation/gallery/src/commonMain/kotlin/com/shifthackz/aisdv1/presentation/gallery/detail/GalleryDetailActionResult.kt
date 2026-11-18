package com.shifthackz.aisdv1.presentation.gallery.detail

/**
 * Completion, unsupported capability or public failure details from a native detail action.
 *
 * @author Dmitriy Moroz
 */
sealed interface GalleryDetailActionResult {
    /**
     * The native operation completed or accepted the share handoff.
     *
     * @author Dmitriy Moroz
     */
    data object Done : GalleryDetailActionResult

    /**
     * This platform adapter cannot perform the requested action.
     *
     * @author Dmitriy Moroz
     */
    data object Unsupported : GalleryDetailActionResult

    /**
     * A native detail action failed with user-facing details.
     *
     * @param message Explanation reported by the platform adapter.
     *
     * @author Dmitriy Moroz
     */
    data class Failed(val message: String) : GalleryDetailActionResult
}
