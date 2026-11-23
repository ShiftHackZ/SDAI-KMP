package com.shifthackz.aisdv1.presentation.backup.preview

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupEntry
import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreResult
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.presentation.backup.BackupPageState
import com.shifthackz.aisdv1.presentation.backup.BackupState

/**
 * Fixed inventory and archive summaries shared by the three backup preview groups.
 * Handles, filenames and provider identifiers are sample metadata; no archives or credentials are read.
 *
 * @author Dmitriy Moroz
 */
internal object BackupPreviewFixtures {
    val providers =
        setOf(ServerSource.AUTOMATIC1111.key, ServerSource.SWARM_UI.key, ServerSource.OPEN_AI.key)
    val categories = BackupCategory.entries.toSet()
    val inventory = BackupInspection(
        handle = "preview-inventory",
        createdAt = 1_700_000_000_000L,
        appVersion = "1.3.0",
        sourcePlatform = "Android",
        entries = listOf(
            BackupEntry(BackupCategory.PROVIDERS, 3),
            BackupEntry(BackupCategory.GALLERY, 24),
            BackupEntry(BackupCategory.NETWORK, 128),
            BackupEntry(BackupCategory.CIVITAI, 2),
            BackupEntry(BackupCategory.SETTINGS, 1),
        ),
        providers = providers,
    )
    val archive = inventory.copy(
        handle = "preview-archive",
        filename = "sdai-backup.sdaibackup",
        sourceLocation = "/storage/emulated/0/Download/sdai-backup.sdaibackup",
    )
    val selection = BackupSelection(categories, providers)
    val createPage = BackupPageState(inspection = inventory, selection = selection)
    val restorePage = BackupPageState(inspection = archive, selection = selection)
    val create = BackupState(create = createPage)
    val restore = BackupState(restore = restorePage, isRestoreTab = true)
    val finished = BackupRestoreResult.Finished(
        completed = categories,
        skippedCount = 2,
        hasFailed = false,
        importedCounts = mapOf(
            BackupCategory.PROVIDERS to 3L,
            BackupCategory.GALLERY to 24L,
            BackupCategory.NETWORK to 128L,
            BackupCategory.CIVITAI to 2L,
            BackupCategory.SETTINGS to 1L,
        ),
    )
}
