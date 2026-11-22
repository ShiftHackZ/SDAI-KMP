package com.shifthackz.aisdv1.feature.backup.codec

import okio.Source
import okio.Buffer
import okio.Timeout

/**
 * Prevents a section decoder from consuming adjacent binary sections.
 *
 * @param source Owned archive source positioned at a section boundary.
 * @param remaining Validated length of the selected section.
 * @author Dmitriy Moroz
 */
internal class BoundedBackupSource(private val source: Source, private var remaining: Long) :
    Source {

    override fun read(sink: Buffer, byteCount: Long): Long {
        require(byteCount >= 0)
        if (remaining == 0L) return -1
        val read = source.read(sink, minOf(byteCount, remaining))
        check(read >= 0) { "Truncated backup section" }
        remaining -= read
        return read
    }

    override fun timeout(): Timeout = source.timeout()

    override fun close() = source.close()
}
