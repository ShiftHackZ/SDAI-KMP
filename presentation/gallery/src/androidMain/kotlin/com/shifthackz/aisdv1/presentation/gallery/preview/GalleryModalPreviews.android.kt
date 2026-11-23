package com.shifthackz.aisdv1.presentation.gallery.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Displays every grid confirmation, archive progress and operation-error overlay in both palettes.
 *
 * @param scenario Named grid overlay with inert destructive and export callbacks.
 * @author Dmitriy Moroz
 */
@Preview(name = "Grid modal · Light", group = "Gallery dialogs", widthDp = 360, heightDp = 800)
@Preview(
    name = "Grid modal · Dark", group = "Gallery dialogs", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GalleryGridModalPreview(
    @PreviewParameter(GalleryModalPreviewParameterProvider::class) scenario: GalleryPreviewCase,
) {
    GalleryPreview(scenario)
}

/**
 * Displays detail deletion confirmation and operation-error overlays in both palettes.
 *
 * @param scenario Named detail overlay with inert confirmation and dismissal callbacks.
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Detail modal · Light",
    group = "Gallery detail dialogs",
    widthDp = 360,
    heightDp = 800
)
@Preview(
    name = "Detail modal · Dark", group = "Gallery detail dialogs", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GalleryDetailModalPreview(
    @PreviewParameter(GalleryDetailModalPreviewParameterProvider::class) scenario: GalleryDetailPreviewCase,
) {
    GalleryDetailPreview(scenario)
}
