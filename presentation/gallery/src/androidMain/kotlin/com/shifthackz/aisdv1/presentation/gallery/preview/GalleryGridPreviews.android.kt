package com.shifthackz.aisdv1.presentation.gallery.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Displays every grid density, selection and availability snapshot in both palettes.
 *
 * @param scenario Named grid snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "Grid · Light", group = "Gallery grid", widthDp = 360, heightDp = 800)
@Preview(
    name = "Grid · Dark", group = "Gallery grid", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GalleryGridStatesPreview(
    @PreviewParameter(GalleryPreviewParameterProvider::class) scenario: GalleryPreviewCase,
) {
    GalleryPreview(scenario)
}

/**
 * Displays the populated selection grid at narrow, wide and enlarged-text sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Gallery grid sizes", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Gallery grid sizes", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Gallery grid sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Gallery grid sizes",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun GalleryGridSizesPreview() {
    GalleryPreview(
        GalleryPreviewCase(
            "Selection", GalleryPreviewFixtures.grid.copy(
                selectionMode = true,
                selection = listOf(1, 3),
                selectedFlags = GalleryPreviewFixtures.items,
            )
        )
    )
}
