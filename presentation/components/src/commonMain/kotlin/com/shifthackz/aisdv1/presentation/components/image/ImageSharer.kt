package com.shifthackz.aisdv1.presentation.components.image

/**
 * Shares original image bytes through the platform share interface.
 *
 * @author Dmitriy Moroz
 */
interface ImageSharer {
    /**
     * Creates a bounded private image file and presents native sharing UI.
     * A successful result describes the native handoff rather than recipient delivery.
     * Cancellation propagates; temporary files follow the platform ownership lifecycle.
     *
     * @param base64 Encoded image, optionally prefixed by a base64 data URI header.
     *
     * @return Native handoff, unavailable platform support or a reported failure.
     *
     * @author Dmitriy Moroz
     */
    suspend fun share(base64: String): ImageShareResult
}
