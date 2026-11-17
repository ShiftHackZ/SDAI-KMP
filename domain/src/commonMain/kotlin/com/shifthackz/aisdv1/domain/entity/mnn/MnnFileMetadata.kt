package com.shifthackz.aisdv1.domain.entity.mnn

/**
 * Exact size of a regular bundle file after platform filesystem inspection succeeds.
 *
 * @param sizeBytes Nonnegative file length in bytes; IO failures never produce metadata.
 *
 * @author Dmitriy Moroz
 */
data class MnnFileMetadata(val sizeBytes: Long)
