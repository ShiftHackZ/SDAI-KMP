package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Durable export/result association written before the native document picker opens.
 * @param handle Generated app-owned staging identity, without a platform path.
 * @param filename Suggested public binary backup filename.
 * @param status Last durably acknowledged export stage.
 * @param destination Opaque selected output reference, retained for interrupted-copy retry.
 * @author Dmitriy Moroz
 */
data class BackupExportIntent(
    val handle: String,
    val filename: String,
    val status: BackupExportStatus = BackupExportStatus.READY,
    val destination: String? = null,
)
