package com.shifthackz.aisdv1.presentation.gallery.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryDialog

/**
 * Supplies every grid confirmation/progress overlay and short or wrapped failure text.
 *
 * @author Dmitriy Moroz
 */
internal class GalleryModalPreviewParameterProvider : PreviewParameterProvider<GalleryPreviewCase> {
    private val dialogs = listOf(
        "Delete all" to GalleryDialog.DeleteAllConfirm,
        "Delete selection" to GalleryDialog.DeleteSelectionConfirm,
        "Export all" to GalleryDialog.ConfirmExport(exportAll = true),
        "Export selection" to GalleryDialog.ConfirmExport(exportAll = false),
        "Export progress" to GalleryDialog.ExportInProgress,
        "Operation error" to GalleryDialog.Error("The image archive could not be created."),
        "Long operation error" to GalleryDialog.Error("The image archive could not be created because there is insufficient free storage. Free some space and try exporting again."),
    )
    private val cases = dialogs.map { (name, dialog) ->
        GalleryPreviewCase(
            name, GalleryPreviewFixtures.grid.copy(
                selectionMode = true,
                selection = listOf(1, 2),
                selectedFlags = GalleryPreviewFixtures.items.take(2),
                dialog = dialog,
            )
        )
    }

    /**
     * Fixed overlay snapshots with inert confirmation and dismissal callbacks.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<GalleryPreviewCase> = cases.asSequence()

    /**
     * Identifies the destructive/export scope, progress or failure condition.
     *
     * @param index Position in the ordered overlay-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
