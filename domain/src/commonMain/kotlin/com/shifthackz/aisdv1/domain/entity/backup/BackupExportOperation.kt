package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Observable application-owned export state; successful status means native completion or completed byte copy.
 * @param status Current durable operation stage.
 * @param backup Resolved private export reference; no archive payload or credentials.
 * @author Dmitriy Moroz
 */
data class BackupExportOperation(
    val status: BackupExportStatus = BackupExportStatus.NONE,
    val backup: BackupExport? = null
)
