package com.shifthackz.aisdv1.presentation.utils

import java.io.File

/**
 * Writes original encoded pixels without bitmap decoding or lossy re-encoding.
 *
 * @author Dmitriy Moroz
 */
internal interface OriginalImageFileExporter {
    /**
     * Streams admitted Base64 original bytes to a cache file without bitmap re-encoding.
     * The signature selects the PNG/JPEG/WebP extension; failures and cancellation remove partial output.
     * Complete image structure is not validated by this byte-preserving exporter.
     *
     * @param base64 Original encoded image, at most 32MiB of text.
     * @param name Application-owned safe file stem.
     * @return Cache file with its actual image extension.
     * @author Dmitriy Moroz
     */
    suspend fun export(base64: String, name: String): File
}
