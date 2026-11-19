package com.shifthackz.aisdv1.feature.backup.restore

/**
 * Identifies exact encoded image bytes independently of Base64 alphabet, whitespace, padding or gallery metadata.
 * @author Dmitriy Moroz
 */
internal fun interface BackupImageFingerprint {
    /**
     * Streams bounded Base64 text into a SHA-256 identity without decoding image pixels.
     * Invalid or absent existing image text has no identity; storage failures and cancellation propagate.
     * @param readChunk Reads sequentially at most the requested UTF8 bytes at an increasing zero-based byte offset; empty means EOF, null means absent.
     * @return Encoded-image byte identity, or null for missing, empty or malformed Base64.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(readChunk: suspend (offset: Long, maxBytes: Int) -> ByteArray?): String?
}
