package com.shifthackz.aisdv1.presentation.gallery.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailDialog

/**
 * Supplies detail deletion confirmation and short or wrapped public operation failures.
 *
 * @author Dmitriy Moroz
 */
internal class GalleryDetailModalPreviewParameterProvider :
    PreviewParameterProvider<GalleryDetailPreviewCase> {
    private val cases = listOf(
        "Delete image" to GalleryDetailDialog.DeleteConfirm,
        "Operation error" to GalleryDetailDialog.Error("The image could not be saved."),
        "Long operation error" to GalleryDetailDialog.Error("The original image is no longer available. Return to the gallery and choose another image before trying this action again."),
    ).map { (name, dialog) ->
        GalleryDetailPreviewCase(name, GalleryPreviewFixtures.detailState().copy(dialog = dialog))
    }

    /**
     * Fixed detail overlays with inert confirmation and dismissal callbacks.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<GalleryDetailPreviewCase> = cases.asSequence()

    /**
     * Identifies the current detail confirmation or failure condition.
     *
     * @param index Position in the ordered overlay-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
