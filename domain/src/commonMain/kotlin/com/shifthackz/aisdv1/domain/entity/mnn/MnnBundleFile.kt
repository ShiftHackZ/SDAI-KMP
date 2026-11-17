package com.shifthackz.aisdv1.domain.entity.mnn

/**
 * Integrity metadata for one immutable bundle payload.
 *
 * @param path Root filename without directory separators.
 * @param size Exact byte count.
 * @param sha256 Lowercase SHA256 of the complete file.
 *
 * @author Dmitriy Moroz
 */
data class MnnBundleFile(val path: String, val size: Long, val sha256: String)
