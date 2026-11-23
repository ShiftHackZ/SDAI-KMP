@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.gallery.detail

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSection
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSubtree
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.rememberGalleryImage
import com.shifthackz.aisdv1.presentation.components.widget.image.DecodedImageBitmap
import com.shifthackz.aisdv1.presentation.components.widget.image.ZoomableImage
import com.shifthackz.aisdv1.presentation.components.widget.image.ZoomableImageSource

/**
 * Renders detail tabs, sampled image paging, metadata and caller-owned operation dialogs.
 *
 * @param modifier Layout modifiers for the detail surface and scaffold.
 * @param state Current detail item, tab and bounded neighbor window.
 * @param imageLoader Revision-aware sampled loader; null renders static-preview fallback content.
 * @param processIntent Requests detail navigation, native actions or metadata mutations.
 * @param civitaiAction App-supplied Civitai publishing action, retaining independent screen-module ownership.
 * @param content Optional detail body receiving the scaffold's padded viewport modifier.
 * @author Dmitriy Moroz
 */
@Composable
fun GalleryDetailScreenContent(
    modifier: Modifier = Modifier,
    state: GalleryDetailState,
    imageLoader: GalleryImageLoader? = null,
    processIntent: (GalleryDetailIntent) -> Unit = {},
    civitaiAction: @Composable () -> Unit = {},
    content: (@Composable (Modifier) -> Unit)? = null,
) {
    Box(modifier = modifier) {
        PlatformScaffold(
            canUnderlapToolbar = state.selectedTab == GalleryDetailTab.INFO,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                PlatformTopAppBar(
                    title = {
                        Text(
                            Localization.string("title_gallery_details"),
                            style = platformToolbarTitleStyle(MaterialTheme.typography.titleLarge),
                        )
                    },
                    navigationIcon = {
                        ToolbarBackButton(
                            onClick = { processIntent(GalleryDetailIntent.NavigateBack) },
                            icon = Icons.AutoMirrored.Outlined.ArrowBack
                        )
                    },
                    actions = {
                        GalleryDetailToolbarAction(
                            selectedTab = state.selectedTab,
                            isEnabled = state.content != null,
                            processIntent = processIntent,
                        )
                    },
                    windowInsets = persistentTopAppBarWindowInsets(),
                )
            },
            content = { paddingValues ->
                val contentModifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)

                if (content != null) {
                    content(contentModifier)
                } else if (state.content != null) {
                    GalleryDetailContentState(
                        modifier = contentModifier,
                        state = state,
                        imageLoader = imageLoader,
                        content = state.content,
                        onCopyTextClick = { value ->
                            processIntent(GalleryDetailIntent.CopyToClipboard(value))
                        },
                        onPageSelected = { page ->
                            processIntent(GalleryDetailIntent.NavigateToPage(page))
                        },
                    )
                }
            },
            bottomBar = {
                GalleryDetailNavigationBar(
                    state = state,
                    civitaiAction = civitaiAction,
                    processIntent = processIntent,
                )
            },
        )
        GalleryDetailDialogRenderer(
            dialog = state.dialog,
            processIntent = processIntent,
        )
    }
}

/**
 * Chooses sampled output/source paging or the metadata table for the active detail tab.
 *
 * @param modifier Layout modifiers for the active detail body.
 * @param state Current tab and bounded paging window.
 * @param content Metadata for the current detail item.
 * @param imageLoader Revision-aware sampled loader for persistent image identities.
 * @param onCopyTextClick Requests native clipboard copying of one metadata value.
 * @param onPageSelected Reports a newly selected global gallery page index.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailContentState(
    modifier: Modifier = Modifier,
    state: GalleryDetailState,
    content: GalleryDetailContent,
    imageLoader: GalleryImageLoader? = null,
    onCopyTextClick: (String) -> Unit = {},
    onPageSelected: (Int) -> Unit = {},
) {
    when (state.selectedTab) {
        GalleryDetailTab.IMAGE -> {
            if (content.id > 0 || content.imageBase64.isNotBlank()) {
                SwipeableGalleryImage(
                    modifier = modifier,
                    selectedTab = state.selectedTab,
                    imageLoader = imageLoader,
                    content = content,
                    pagerContents = state.pagerContents,
                    pagerContentStartIndex = state.pagerContentStartIndex,
                    pagerCurrentIndex = state.pagerCurrentIndex,
                    pagerPageCount = state.galleryItemIds.size,
                    onPageSelected = onPageSelected,
                )
            }
        }

        GalleryDetailTab.ORIGINAL -> {
            if (content.inputImageBase64 != null) {
                SwipeableGalleryImage(
                    modifier = modifier,
                    selectedTab = state.selectedTab,
                    imageLoader = imageLoader,
                    content = content,
                    pagerContents = state.pagerContents,
                    pagerContentStartIndex = state.pagerContentStartIndex,
                    pagerCurrentIndex = state.pagerCurrentIndex,
                    pagerPageCount = state.galleryItemIds.size,
                    onPageSelected = onPageSelected,
                )
            }
        }

        GalleryDetailTab.INFO -> GalleryDetailsTable(
            modifier = modifier,
            content = content,
            onCopyTextClick = onCopyTextClick,
        )
    }
}

/**
 * Pages sampled gallery images while preventing page swipes during image zoom.
 * Only the supplied bounded metadata window is retained; persistent pixels use the sampled loader.
 *
 * @param modifier Layout modifiers for the pager viewport.
 * @param selectedTab Selects generated output or original-source pixels.
 * @param imageLoader Revision-aware loader for persistent image content.
 * @param content Current detail metadata used when a neighbor is unavailable.
 * @param pagerContents Bounded current/neighbor metadata window.
 * @param pagerContentStartIndex Global gallery index corresponding to the first window entry.
 * @param pagerCurrentIndex Current global gallery index.
 * @param pagerPageCount Total number of gallery identities available for navigation.
 * @param onPageSelected Requests a different global gallery index after a page change.
 * @author Dmitriy Moroz
 */
@Composable
private fun SwipeableGalleryImage(
    modifier: Modifier = Modifier,
    selectedTab: GalleryDetailTab,
    imageLoader: GalleryImageLoader? = null,
    content: GalleryDetailContent,
    pagerContents: List<GalleryDetailContent>,
    pagerContentStartIndex: Int,
    pagerCurrentIndex: Int,
    pagerPageCount: Int,
    onPageSelected: (Int) -> Unit = {},
) {
    UiDiagnosticSection(UiDiagnosticSubtree.GALLERY_DETAIL_IMAGE) {
        val pageCount = pagerPageCount.takeIf { it > 0 } ?: pagerContents.size.coerceAtLeast(1)
        val currentPage = pagerCurrentIndex.coerceIn(0, pageCount - 1)
        val pagerState = rememberPagerState(
            initialPage = currentPage,
            pageCount = { pageCount },
        )
        val currentImageScale = remember(content.id, selectedTab) { mutableFloatStateOf(1f) }

        LaunchedEffect(selectedTab, currentPage) {
            currentImageScale.floatValue = 1f
            if (pagerState.currentPage != currentPage) {
                pagerState.scrollToPage(currentPage)
            }
        }

        LaunchedEffect(pagerState.settledPage, currentPage, pagerContentStartIndex, pagerContents) {
            if (pagerState.settledPage != currentPage) {
                onPageSelected(pagerState.settledPage)
            }
        }

        HorizontalPager(
            modifier = modifier,
            state = pagerState,
            userScrollEnabled = currentImageScale.floatValue <= GALLERY_PAGER_ZOOM_LOCK_SCALE,
        ) { page ->
            val pageContent = pagerContents.getOrNull(page - pagerContentStartIndex)
            if (pageContent == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                )
                return@HorizontalPager
            }
            val base64 = pageContent.selectedImageBase64OrNull(selectedTab)
            if (pageContent.id <= 0 && base64 == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                )
                return@HorizontalPager
            }

            val isCurrentPage = pageContent.id == content.id
            var imageEdge by remember(pageContent.id, selectedTab) { mutableIntStateOf(0) }
            val decodedImage = if (imageLoader != null) {
                rememberGalleryImage(
                    imageLoader, pageContent.id, pageContent.imageRevision,
                    selectedTab == GalleryDetailTab.ORIGINAL, imageEdge,
                    transientBase64 = base64.takeIf { pageContent.id <= 0 }).value
            } else DecodedImageBitmap.Unavailable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { imageEdge = maxOf(it.width, it.height) }
                    .background(MaterialTheme.colorScheme.background),
            ) {
                when (val image = decodedImage) {
                    DecodedImageBitmap.Loading -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmer(),
                    )

                    is DecodedImageBitmap.Ready -> ZoomableImage(
                        modifier = Modifier.fillMaxSize(),
                        source = ZoomableImageSource.Bitmap(image.image),
                        hideImage = pageContent.hidden && selectedTab == GalleryDetailTab.IMAGE,
                        fitToWidth = true,
                        gesturesEnabled = isCurrentPage,
                        onScaleChange = { scale ->
                            if (isCurrentPage) {
                                currentImageScale.floatValue = scale
                            }
                        },
                    )

                    DecodedImageBitmap.Unavailable -> Unit
                }
            }
        }
    }
}


private fun GalleryDetailContent.selectedImageBase64OrNull(selectedTab: GalleryDetailTab): String? =
    when (selectedTab) {
        GalleryDetailTab.IMAGE -> imageBase64.takeIf(String::isNotBlank)
        GalleryDetailTab.ORIGINAL -> inputImageBase64
        GalleryDetailTab.INFO -> null
    }

private const val GALLERY_PAGER_ZOOM_LOCK_SCALE = 1.01f
