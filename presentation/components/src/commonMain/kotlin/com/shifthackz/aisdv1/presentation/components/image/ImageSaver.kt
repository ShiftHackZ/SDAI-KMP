package com.shifthackz.aisdv1.presentation.components.image

/**
 * Saves original encoded image content to the platform media library.
 *
 * @author Dmitriy Moroz
 */
interface ImageSaver {
    /**
     * Requests native permission when needed and saves the original bytes.
     * Cancellation propagates, but cannot undo a native library write already begun.
     *
     * @param base64 Encoded image, optionally prefixed by a base64 data URI header.
     *
     * @return Completion, unavailable platform support or a reported failure.
     *
     * @author Dmitriy Moroz
     */
    suspend fun save(base64: String): ImageSaveResult
}
