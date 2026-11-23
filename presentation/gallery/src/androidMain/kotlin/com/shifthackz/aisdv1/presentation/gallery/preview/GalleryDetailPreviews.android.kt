package com.shifthackz.aisdv1.presentation.gallery.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailTab

/**
 * Displays detail tabs, image geometry and metadata variants in both palettes.
 *
 * @param scenario Named detail snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "Detail · Light", group = "Gallery detail", widthDp = 360, heightDp = 800)
@Preview(
    name = "Detail · Dark", group = "Gallery detail", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GalleryDetailStatesPreview(
    @PreviewParameter(GalleryDetailPreviewParameterProvider::class) scenario: GalleryDetailPreviewCase,
) {
    GalleryDetailPreview(scenario)
}

/**
 * Displays the metadata table and persistent detail actions at narrow, wide and enlarged-text sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Gallery detail sizes", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Gallery detail sizes", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Gallery detail sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Gallery detail sizes",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun GalleryDetailSizesPreview() {
    GalleryDetailPreview(
        GalleryDetailPreviewCase(
            "Metadata", GalleryPreviewFixtures.detailState(tab = GalleryDetailTab.INFO),
        )
    )
}
