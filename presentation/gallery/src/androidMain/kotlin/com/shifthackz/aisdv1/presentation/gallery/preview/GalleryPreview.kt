package com.shifthackz.aisdv1.presentation.gallery.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.image.DecodedImageBitmap
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryGridImageContent
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryScreenContent

/**
 * Displays the real grid, flags, selection bars and dialogs using fixed pixels and inert intents.
 * No ViewModel, background-work observer, storage reader or image loader is created.
 *
 * @param scenario Gallery metadata, selection, pixel availability and initial scroll position.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryPreview(scenario: GalleryPreviewCase) {
    val image = rememberGalleryPreviewBitmap()
    GalleryPreviewTheme {
        GalleryScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = scenario.state,
            processIntent = {},
            listState = rememberLazyGridState(initialFirstVisibleItemIndex = scenario.firstVisibleItem),
            backgroundWorkContent = {},
            imageContent = { item, borderColor ->
                GalleryGridImageContent(
                    item = item,
                    borderColor = borderColor,
                    decodedImage = when {
                        scenario.isImageLoading -> DecodedImageBitmap.Loading
                        item.id in scenario.unavailableImageIds -> DecodedImageBitmap.Unavailable
                        else -> DecodedImageBitmap.Ready(image)
                    },
                )
            },
        )
    }
}
