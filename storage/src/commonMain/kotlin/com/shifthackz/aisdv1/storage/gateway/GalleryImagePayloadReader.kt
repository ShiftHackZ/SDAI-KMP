package com.shifthackz.aisdv1.storage.gateway

/**
 * Reads one admitted encoded-image payload without exposing platform database handles.
 *
 * @author Dmitriy Moroz
 */
interface GalleryImagePayloadReader {
    /**
     * Reads UTF8 Base64 bytes only for the requested revision and encoded-size budget.
     * Missing, replaced or oversized content returns null; database errors and cancellation propagate.
     *
     * @param id Saved image identity.
     * @param revision Expected immutable content revision.
     * @param isInput Whether to select the original input rather than the result.
     * @param maxChars Positive encoded character/ASCII byte limit.
     * @author Dmitriy Moroz
     */
    suspend fun read(id: Long, revision: String, isInput: Boolean, maxChars: Int): ByteArray?
}
