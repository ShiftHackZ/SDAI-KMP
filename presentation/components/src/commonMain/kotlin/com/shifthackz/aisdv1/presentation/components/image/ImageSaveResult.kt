package com.shifthackz.aisdv1.presentation.components.image

/**
 * Outcome reported by the native original-image save adapter.
 *
 * @author Dmitriy Moroz
 */
sealed interface ImageSaveResult {
    /** The native media library accepted the image write. */
    data object Saved : ImageSaveResult

    /** The current platform adapter cannot perform this operation. */
    data object Unsupported : ImageSaveResult

    /**
     * Permission, decoding, file or native presentation failure reported to the UI.
     *
     * @param message Explanation supplied by the platform adapter.
     *
     * @author Dmitriy Moroz
     */
    data class Failed(val message: String) : ImageSaveResult
}
