package com.shifthackz.aisdv1.presentation.gallery.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.presentation.components.widget.image.ZoomableImage
import com.shifthackz.aisdv1.presentation.components.widget.image.ZoomableImageSource
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailScreenContent
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailTab
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailsTable

/**
 * Displays the real detail chrome, metadata table or image widget without persistent paging/loading.
 * Copy, tab, mutation and operation callbacks remain inert; Civitai publishing belongs to its own module.
 *
 * @param scenario Detail state, sample image geometry and metadata scroll position.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailPreview(scenario: GalleryDetailPreviewCase) {
    val image = rememberGalleryPreviewBitmap(scenario.imageWidth, scenario.imageHeight)
    GalleryPreviewTheme {
        GalleryDetailScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = scenario.state,
            processIntent = {},
            content = { bodyModifier ->
                scenario.state.content?.let { content ->
                    when {
                        scenario.state.selectedTab == GalleryDetailTab.INFO -> GalleryDetailsTable(
                            modifier = bodyModifier,
                            content = content,
                            onCopyTextClick = {},
                            scrollState = rememberScrollState(scenario.metadataOffset),
                        )

                        scenario.isImageLoading -> Box(bodyModifier.fillMaxSize().shimmer())
                        scenario.isImageUnavailable -> Box(bodyModifier.fillMaxSize())
                        else -> ZoomableImage(
                            modifier = bodyModifier,
                            source = ZoomableImageSource.Bitmap(image),
                            hideImage = content.hidden && scenario.state.selectedTab == GalleryDetailTab.IMAGE,
                            fitToWidth = true,
                            gesturesEnabled = false,
                        )
                    }
                }
            },
        )
    }
}
