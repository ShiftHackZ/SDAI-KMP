package com.shifthackz.aisdv1.presentation.backup.preview

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreNotice
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreResult
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.presentation.backup.BackupState

/**
 * Restore snapshots reused with first-launch chrome and with the normal two-tab screen.
 * Dialogs receive inert event sinks through the preview wrapper, so no restore is submitted.
 *
 * @author Dmitriy Moroz
 */
internal object BackupRestorePreviewCases {
    private val ready = BackupPreviewFixtures.restore
    private val page = BackupPreviewFixtures.restorePage
    private val finished = ready.copy(
        result = BackupPreviewFixtures.finished,
        isRestoreSelectionLocked = true,
        isFeedbackVisible = true,
    )
    val values = listOf(
        BackupPreviewCase("Choose a file", BackupState(isRestoreTab = true)),
        BackupPreviewCase("Picker open", BackupState(isRestoreTab = true, isPicking = true)),
        BackupPreviewCase("Reading archive", ready.copy(isBusy = true)),
        BackupPreviewCase("Archive selected", ready),
        BackupPreviewCase(
            "No categories selected",
            ready.copy(restore = page.copy(selection = BackupSelection()))
        ),
        BackupPreviewCase(
            "Some providers selected", ready.copy(
                restore = page.copy(
                    selection = BackupSelection(
                        categories = setOf(BackupCategory.PROVIDERS, BackupCategory.GALLERY),
                        providers = setOf(ServerSource.AUTOMATIC1111.key),
                    )
                ),
            )
        ),
        BackupPreviewCase(
            "Providers collapsed",
            ready.copy(restore = page.copy(areProvidersExpanded = false))
        ),
        BackupPreviewCase("Lower categories", ready, scrollOffset = 100_000),
        BackupPreviewCase(
            "Unsupported entries",
            ready.copy(
                restore = page.copy(
                    inspection = BackupPreviewFixtures.archive.copy(
                        entries = BackupPreviewFixtures.archive.entries.map {
                            if (it.category == BackupCategory.CIVITAI) it.copy(isAvailable = false) else it
                        },
                        skippedCount = 4,
                    ),
                    selection = BackupPreviewFixtures.selection.copy(
                        categories = BackupPreviewFixtures.categories - BackupCategory.CIVITAI,
                    ),
                )
            ),
        ),
        BackupPreviewCase(
            "Long archive location",
            ready.copy(
                restore = page.copy(
                    inspection = BackupPreviewFixtures.archive.copy(
                        filename = "sdai-backup-all-providers-and-gallery-before-device-transfer.sdaibackup",
                        sourceLocation = "/storage/emulated/0/Download/Archived backups/Device transfer/sdai-backup-all-providers-and-gallery-before-device-transfer.sdaibackup",
                    ),
                )
            ),
        ),
        BackupPreviewCase(
            "Selection locked",
            ready.copy(isRestoreSelectionLocked = true, hasPendingSession = true)
        ),
        BackupPreviewCase(
            "Restoring gallery",
            ready.copy(
                isBusy = true, isRestoreSelectionLocked = true,
                progress = BackupProgress(BackupCategory.GALLERY, completed = 12, total = 24),
            ),
        ),
        BackupPreviewCase(
            "Progress without total",
            ready.copy(
                isBusy = true, progress = BackupProgress(category = null, completed = 3, total = 0),
            ),
        ),
        BackupPreviewCase(
            "Provider replacement consent",
            ready.copy(
                isRestoreSelectionLocked = true,
                conflict = BackupRestoreResult.Conflict(
                    BackupPreviewFixtures.providers,
                    "preview-consent"
                ),
            ),
        ),
        BackupPreviewCase("Restore complete", finished),
        BackupPreviewCase("Completion acknowledged", finished.copy(isFeedbackVisible = false)),
        BackupPreviewCase(
            "Partial restore",
            finished.copy(
                result = BackupRestoreResult.Finished(
                    completed = setOf(BackupCategory.GALLERY), skippedCount = 3, hasFailed = true,
                    importedCounts = mapOf(BackupCategory.GALLERY to 24L),
                )
            ),
        ),
        BackupPreviewCase("Canceled", ready.copy(hasCanceled = true, isFeedbackVisible = true)),
        BackupPreviewCase("Failed", ready.copy(hasError = true, isFeedbackVisible = true)),
        BackupPreviewCase(
            "Shared credentials retained",
            finished.copy(
                result = BackupPreviewFixtures.finished.copy(
                    notices = listOf(
                        BackupRestoreNotice.SharedCredentialsRetained(
                            setOf(
                                ServerSource.SWARM_UI.key
                            )
                        )
                    ),
                )
            ),
        ),
    )
}
