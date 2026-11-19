package com.shifthackz.aisdv1.data.performance

/**
 * Reads actual unused RAM from Linux meminfo without treating reclaimable caches as free.
 * Invalid or missing counters fail the sample rather than changing the accounting definition.
 *
 * @param content Complete /proc/meminfo text, whose memory quantities use binary kilobytes.
 * @param totalBytes Device physical capacity used to reject inconsistent kernel counters.
 * @author Dmitriy Moroz
 */
internal fun parseFreeMemoryBytes(content: String, totalBytes: Long): Long {
    val fields = content.lineSequence().first { it.startsWith("MemFree:") }
        .trim().split(Regex("\\s+"))
    require(fields.size == 3 && fields[2] == "kB")
    val kibibytes = fields[1].toLong()
    require(totalBytes > 0 && kibibytes in 0..totalBytes / 1024)
    return kibibytes * 1024
}
