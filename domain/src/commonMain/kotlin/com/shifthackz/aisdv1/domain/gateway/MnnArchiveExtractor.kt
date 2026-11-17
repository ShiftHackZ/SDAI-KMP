package com.shifthackz.aisdv1.domain.gateway

/**
 * Extracts ten bounded regular root files into a new private directory.
 *
 * @author Dmitriy Moroz
 */
interface MnnArchiveExtractor {
    /**
     * Rejects duplicate, linked, traversing and oversized entries before activation.
     *
     * @param archivePath Completed private ZIP.
     * @param stagingPath Empty owned destination removed by the caller after failure.
     * @throws Exception when ZIP validation, extraction or disk allocation fails.
     *
     * @author Dmitriy Moroz
     */
    @Throws(Exception::class)
    fun extract(archivePath: String, stagingPath: String)
}
