package com.shifthackz.aisdv1.feature.backup.codec

import okio.Path
import okio.BufferedSink
import okio.BufferedSource

/**
 * Framing and integrity operations for the portable binary container.
 * @author Dmitriy Moroz
 */
internal interface BackupArchiveCodec {
    /**
     * Writes a complete binary envelope from precomputed streamed sections; cancellation or write failure propagates.
     * @param path Private target archive.
     * @param manifest Descriptors whose lengths and digests match payload files.
     * @param payloads Private section files in manifest order.
     * @author Dmitriy Moroz
     */
    suspend fun write(path: Path, manifest: BackupManifest, payloads: List<Path>)

    /**
     * Checks framing, supported container major, lengths and every section digest before returning metadata.
     * @param path Immutable staged archive; malformed or truncated input throws.
     * @author Dmitriy Moroz
     */
    suspend fun inspect(path: Path): BackupManifest

    /**
     * Opens a bounded reader that cannot consume the next section; the caller owns closing it.
     * @param path Validated staged archive.
     * @param manifest Verified descriptor order and lengths.
     * @param id Known section identifier to read.
     * @author Dmitriy Moroz
     */
    fun openSection(path: Path, manifest: BackupManifest, id: String): BufferedSource

    /**
     * Computes the streamed SHA-256 and size descriptor without retaining the payload in memory.
     * @param path Completed private section file.
     * @param id Stable section wire key.
     * @param count Number of records actually encoded.
     * @author Dmitriy Moroz
     */
    suspend fun describe(path: Path, id: String, count: Long): BackupSection

    /**
     * Writes one bounded UTF8 metadata frame, rejecting records over the metadata limit.
     * @param sink Owned binary section writer.
     * @param text Typed metadata JSON, excluding binary image data.
     * @author Dmitriy Moroz
     */
    fun writeJson(sink: BufferedSink, text: String)

    /**
     * Reads exactly one bounded metadata frame; invalid length or truncation throws.
     * @param source Reader confined to the selected section.
     * @author Dmitriy Moroz
     */
    fun readJson(source: BufferedSource): String
}
