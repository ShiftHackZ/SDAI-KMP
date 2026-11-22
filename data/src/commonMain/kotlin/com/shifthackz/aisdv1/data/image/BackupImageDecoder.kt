package com.shifthackz.aisdv1.data.image

/**
 * Backup-specific native probing and bounded content decoding; never materializes the original bitmap.
 *
 * @author Dmitriy Moroz
 */
internal interface BackupImageDecoder {
    /**
     * Reads original pixel dimensions through metadata-only platform APIs before pixel allocation.
     *
     * @param bytes Encoded image already admitted by the shared byte budget.
     * @author Dmitriy Moroz
     */
    fun dimensions(bytes: ByteArray): Pair<Long, Long>

    /**
     * Rejects undecodable content by creating and immediately releasing a bounded native sample.
     *
     * @param bytes Encoded image whose original dimensions passed admission.
     * @param maxEdge Maximum sample width and height in pixels.
     * @author Dmitriy Moroz
     */
    fun validateSample(bytes: ByteArray, maxEdge: Int)
}
