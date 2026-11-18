package com.shifthackz.aisdv1.presentation.gallery.detail

/**
 * Native media, sharing and clipboard operations used by the gallery-detail route.
 *
 * @author Dmitriy Moroz
 */
interface GalleryDetailPlatformActions {
    /**
     * Saves original image bytes through the native media library adapter.
     *
     * @param base64 Original encoded image, optionally prefixed by a Base64 data URI.
     *
     * @return Native completion/support/failure status; cancellation is not converted into success.
     *
     * @author Dmitriy Moroz
     */
    suspend fun saveImage(base64: String): GalleryDetailActionResult

    /**
     * Stages original image bytes and presents native sharing.
     *
     * @param base64 Original encoded image, optionally prefixed by a Base64 data URI.
     *
     * @return Done after native handoff, which does not establish recipient delivery.
     *
     * @author Dmitriy Moroz
     */
    suspend fun shareImage(base64: String): GalleryDetailActionResult

    /**
     * Presents native text sharing for generation parameters or prompts.
     *
     * @param text Already formatted user-visible text to share.
     *
     * @return Native handoff/support/failure status.
     *
     * @author Dmitriy Moroz
     */
    suspend fun shareText(text: String): GalleryDetailActionResult

    /**
     * Copies formatted generation text to the platform clipboard.
     *
     * @param text Prompt or metadata text to copy.
     *
     * @return Clipboard operation/support/failure status.
     *
     * @author Dmitriy Moroz
     */
    suspend fun copyText(text: String): GalleryDetailActionResult
}
