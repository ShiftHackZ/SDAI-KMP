@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.widget.dialog.DecisionInteractiveDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog


/**
 * Renders confirmed deletion or a public operation failure above gallery detail.
 *
 * @param dialog Current confirmation/error overlay selected by the ViewModel.
 * @param processIntent Requests confirmed deletion or dismissal.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailDialogRenderer(
    dialog: GalleryDetailDialog,
    processIntent: (GalleryDetailIntent) -> Unit,
) {
    when (dialog) {
        GalleryDetailDialog.None -> Unit
        GalleryDetailDialog.DeleteConfirm -> DecisionInteractiveDialog(
            title = Localization.string("interaction_delete_generation_title").asUiText(),
            text = Localization.string("interaction_delete_generation_sub_title").asUiText(),
            confirmActionText = Localization.string("yes").asUiText(),
            dismissActionText = Localization.string("no").asUiText(),
            onConfirmAction = { processIntent(GalleryDetailIntent.Delete.Confirm) },
            onDismissRequest = { processIntent(GalleryDetailIntent.DismissDialog) },
        )

        is GalleryDetailDialog.Error -> ErrorDialog(
            text = dialog.message.asUiText(),
            onDismissRequest = { processIntent(GalleryDetailIntent.DismissDialog) },
        )
    }
}

/**
 * Resolves the current localized label of an output/source/info tab.
 *
 * @author Dmitriy Moroz
 */
internal val GalleryDetailTab.label: String
    get() = when (this) {
        GalleryDetailTab.IMAGE -> Localization.string("gallery_tab_image")
        GalleryDetailTab.ORIGINAL -> Localization.string("gallery_tab_original")
        GalleryDetailTab.INFO -> Localization.string("gallery_tab_info")
    }

/**
 * Selects the image or information icon for a detail tab.
 *
 * @author Dmitriy Moroz
 */
internal val GalleryDetailTab.icon: ImageVector
    get() = when (this) {
        GalleryDetailTab.IMAGE -> Icons.Default.Image
        GalleryDetailTab.ORIGINAL -> Icons.Default.Image
        GalleryDetailTab.INFO -> Icons.Default.Info
    }
