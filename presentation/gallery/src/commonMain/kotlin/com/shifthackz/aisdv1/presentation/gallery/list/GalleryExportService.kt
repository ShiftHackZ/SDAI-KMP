package com.shifthackz.aisdv1.presentation.gallery.list

/**
 * Creates gallery archives from persistent generation content without loading originals into screen state.
 *
 * @author Dmitriy Moroz
 */
interface GalleryExportService {
    /**
     * Exports selected generations or the entire gallery to an app-owned archive.
     * Filesystem/read failures and cancellation propagate to the caller.
     *
     * @param ids Persistent identities to include, or null to export all saved generations.
     *
     * @return Completed ZIP artifact available for native sharing.
     *
     * @author Dmitriy Moroz
     */
    suspend fun export(ids: List<Long>? = null): GalleryExportResult
}
