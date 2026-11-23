package com.shifthackz.aisdv1.presentation.gallery.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.MediaStoreInfo
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryState

/**
 * Supplies every grid density, metadata/pixel availability, selection aggregate and media banner.
 *
 * @author Dmitriy Moroz
 */
internal class GalleryPreviewParameterProvider : PreviewParameterProvider<GalleryPreviewCase> {
    private val grid = GalleryPreviewFixtures.grid
    private val items = GalleryPreviewFixtures.items
    private val selected =
        grid.copy(selectionMode = true, selection = listOf(1, 3, 5), selectedFlags = items)
    private val cases = Grid.entries.flatMap { density ->
        listOf(
            GalleryPreviewCase("Loading · ${density.size} columns", GalleryState(grid = density)),
            GalleryPreviewCase("Populated · ${density.size} columns", grid.copy(grid = density)),
        )
    } + listOf(
        GalleryPreviewCase("Empty", GalleryState(loading = false)),
        GalleryPreviewCase("Single image", grid.copy(items = items.take(1), itemIds = listOf(1))),
        GalleryPreviewCase("Metadata window loading", grid.copy(items = items.take(1))),
        GalleryPreviewCase("Pixels loading", grid, isImageLoading = true),
        GalleryPreviewCase("Unavailable images", grid, unavailableImageIds = setOf(1, 2, 3, 4)),
        GalleryPreviewCase("Selection · None", grid.copy(selectionMode = true)),
        GalleryPreviewCase("Selection · Mixed flags", selected),
        GalleryPreviewCase("Selection · All hidden", selected.copy(selection = listOf(5, 10))),
        GalleryPreviewCase("Selection · All liked", selected.copy(selection = listOf(3, 6))),
        GalleryPreviewCase(
            "Selection outside metadata window", selected.copy(
                items = items.take(4), selection = listOf(25), selectedFlags = listOf(items[24]),
            )
        ),
        GalleryPreviewCase(
            "Media export banner",
            grid.copy(mediaStoreInfo = MediaStoreInfo(count = 3))
        ),
        GalleryPreviewCase("Menu", grid.copy(dropdownMenuShow = true)),
        GalleryPreviewCase(
            "Menu · Media folder", grid.copy(
                dropdownMenuShow = true, mediaStoreInfo = MediaStoreInfo(count = 3),
            )
        ),
        GalleryPreviewCase("Scrolled grid", grid, firstVisibleItem = 16),
    )

    /**
     * Fixed grid snapshots rendered in both application palettes.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<GalleryPreviewCase> = cases.asSequence()

    /**
     * Identifies the density, selection, menu or loading condition.
     *
     * @param index Position in the ordered grid-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
