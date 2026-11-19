package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Complete staged backup ready for the native document export flow.
 * @param document Opaque platform document reference, never shown as UI text.
 * @param filename Suggested public filename ending in .sdaibackup.
 * @param handle Generated staging identity persisted before document presentation.
 * @author Dmitriy Moroz
 */
data class BackupExport(val document: String, val filename: String, val handle: String)
