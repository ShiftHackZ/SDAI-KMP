package com.shifthackz.aisdv1.presentation.components.image

/**
 * Outcome reported by the native original-image share adapter.
 *
 * @author Dmitriy Moroz
 */
sealed interface ImageShareResult {
    /** Native sharing accepted the handoff; recipient delivery is not guaranteed. */
    data object Sent : ImageShareResult

    /** The current platform adapter cannot perform this operation. */
    data object Unsupported : ImageShareResult

    /**
     * Permission, decoding, file or native presentation failure reported to the UI.
     *
     * @param message Explanation supplied by the platform adapter.
     *
     * @author Dmitriy Moroz
     */
    data class Failed(val message: String) : ImageShareResult
}
