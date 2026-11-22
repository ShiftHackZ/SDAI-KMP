package com.shifthackz.aisdv1.data.image

/**
 * Supplies native metadata-only probing and sampled validation on each supported platform.
 *
 * @author Dmitriy Moroz
 */
internal expect class PlatformBackupImageDecoder() : BackupImageDecoder {
    /**
     * Reads encoded image dimensions without allocating the original bitmap.
     *
     * @param bytes Encoded image admitted by the shared backup byte budget.
     * @author Dmitriy Moroz
     */
    override fun dimensions(bytes: ByteArray): Pair<Long, Long>

    /**
     * Decodes and releases a bounded sample, rejecting invalid image content.
     *
     * @param bytes Encoded image whose original dimensions passed admission.
     * @param maxEdge Maximum sample width and height in pixels.
     * @author Dmitriy Moroz
     */
    override fun validateSample(bytes: ByteArray, maxEdge: Int)
}
