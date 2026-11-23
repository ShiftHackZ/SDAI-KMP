package com.shifthackz.aisdv1.presentation.backup.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.presentation.backup.BackupState

/**
 * Supplies both selected tabs, independent checklist states and export/restore feedback.
 * Every scenario retains the normal two-tab chrome rather than rendering a page in isolation.
 *
 * @author Dmitriy Moroz
 */
internal class BackupSettingsPreviewParameterProvider :
    PreviewParameterProvider<BackupPreviewCase> {
    private val ready = BackupPreviewFixtures.create
    private val page = BackupPreviewFixtures.createPage
    private val cases = listOf(
        BackupPreviewCase("Create · Inventory pending", BackupState()),
        BackupPreviewCase("Create · All selected", ready),
        BackupPreviewCase(
            "Create · None selected",
            ready.copy(create = page.copy(selection = BackupSelection()))
        ),
        BackupPreviewCase(
            "Create · Some providers",
            ready.copy(
                create = page.copy(
                    selection = BackupSelection(
                        categories = setOf(BackupCategory.PROVIDERS),
                        providers = setOf(ServerSource.AUTOMATIC1111.key),
                    )
                )
            ),
        ),
        BackupPreviewCase(
            "Create · Providers collapsed",
            ready.copy(create = page.copy(areProvidersExpanded = false))
        ),
        BackupPreviewCase(
            "Create · Lower categories and disclosure",
            ready,
            scrollOffset = 100_000
        ),
        BackupPreviewCase(
            "Create · Empty inventory",
            ready.copy(
                create = page.copy(
                    inspection = BackupPreviewFixtures.inventory.copy(
                        entries = BackupPreviewFixtures.inventory.entries.map { it.copy(itemCount = 0) },
                        providers = emptySet(),
                    ),
                    selection = BackupSelection(),
                )
            ),
        ),
        BackupPreviewCase(
            "Create · Building archive",
            ready.copy(
                isBusy = true, progress = BackupProgress(BackupCategory.GALLERY, 12, 24),
            ),
        ),
        BackupPreviewCase(
            "Create · Destination picker",
            ready.copy(isPicking = true, hasPendingExport = true)
        ),
        BackupPreviewCase("Create · Pending export", ready.copy(hasPendingExport = true)),
        BackupPreviewCase(
            "Create · Exporting",
            ready.copy(isExporting = true, hasPendingExport = true)
        ),
        BackupPreviewCase(
            "Create · Exported",
            ready.copy(hasExported = true, isFeedbackVisible = true)
        ),
        BackupPreviewCase("Create · Export acknowledged", ready.copy(hasExported = true)),
        BackupPreviewCase(
            "Create · Canceled",
            ready.copy(hasCanceled = true, isFeedbackVisible = true)
        ),
        BackupPreviewCase("Create · Failed", ready.copy(hasError = true, isFeedbackVisible = true)),
    ) + BackupRestorePreviewCases.values.map { it.copy(name = "Restore · ${it.name}") }

    /**
     * Named create and restore snapshots rendered by both palette annotations.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<BackupPreviewCase> = cases.asSequence()

    /**
     * Identifies the selected tab and its current checklist or operation state.
     *
     * @param index Position in the ordered two-tab scenario list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
