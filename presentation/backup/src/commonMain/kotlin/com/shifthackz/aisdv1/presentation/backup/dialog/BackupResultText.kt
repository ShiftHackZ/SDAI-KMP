package com.shifthackz.aisdv1.presentation.backup.dialog

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreNotice
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreResult
import com.shifthackz.aisdv1.presentation.backup.component.providerName

/**
 * Formats the committed category counts and skipped-data notices for the shared result dialog.
 *
 * @param result Durable outcome of the submitted restore session.
 * @author Dmitriy Moroz
 */
internal fun backupResultText(result: BackupRestoreResult.Finished): String = buildList {
    if (result.hasFailed) add(Localization.string("backup_partial"))
    result.importedCounts.forEach { (category, count) ->
        add(Localization.string("backup_category_" + category.key) + ": " + count)
    }
    result.notices.forEach { notice ->
        when (notice) {
            is BackupRestoreNotice.SharedCredentialsRetained -> add(
                Localization.string(
                    "backup_shared_credentials_retained",
                    notice.providers.joinToString(", ", transform = ::providerName),
                )
            )
        }
    }
    if (result.skippedCount > 0) add(Localization.string("backup_skipped", result.skippedCount))
}.joinToString("\n\n")
