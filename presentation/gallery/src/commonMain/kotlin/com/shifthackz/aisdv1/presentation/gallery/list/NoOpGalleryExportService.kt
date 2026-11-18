package com.shifthackz.aisdv1.presentation.gallery.list

/**
 * Rejects archive creation on platforms without a gallery exporter.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpGalleryExportService : GalleryExportService {
    /**
     * Reports unavailable gallery export instead of returning a misleading empty artifact.
     *
     * @param ids Requested selection, ignored because this adapter has no exporter.
     *
     * @throws IllegalStateException Always, because archive creation is unavailable.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun export(ids: List<Long>?): GalleryExportResult =
        error("Gallery export is not available on this platform yet.")
}
