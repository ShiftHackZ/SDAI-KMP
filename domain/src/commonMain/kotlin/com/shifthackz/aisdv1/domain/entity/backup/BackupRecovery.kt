package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Nonsecret restore state retained across application restarts.
 * @param request Original operation and selection binding.
 * @param inspection Revalidated staged archive summary.
 * @author Dmitriy Moroz
 */
data class BackupRecovery(val request: BackupRestoreRequest, val inspection: BackupInspection)
