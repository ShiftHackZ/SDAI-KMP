@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.gallery.generated.resources.Res
import com.shifthackz.aisdv1.presentation.gallery.generated.resources.ic_share
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.rememberGalleryImage
import com.shifthackz.aisdv1.presentation.components.widget.image.DecodedImageBitmap
import com.shifthackz.aisdv1.presentation.components.widget.image.ZoomableImage
import com.shifthackz.aisdv1.presentation.components.widget.image.ZoomableImageSource
import org.jetbrains.compose.resources.painterResource

/**
 * Renders detail tabs, sampled image paging, metadata and caller-owned operation dialogs.
 *
 * @param modifier Layout modifiers for the detail surface and scaffold.
 * @param state Current detail item, tab and bounded neighbor window.
 * @param imageLoader Revision-aware sampled loader; null renders static-preview fallback content.
 * @param processIntent Requests detail navigation, native actions or metadata mutations.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun GalleryDetailScreenContent(
    modifier: Modifier = Modifier,
    state: GalleryDetailState,
    imageLoader: GalleryImageLoader? = null,
    processIntent: (GalleryDetailIntent) -> Unit = {},
) {
    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(Localization.string("title_gallery_details"))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { processIntent(GalleryDetailIntent.NavigateBack) },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = Localization.string("action_back"),
                            )
                        }
                    },
                    actions = {
                        AnimatedVisibility(
                            visible = state.content != null && state.selectedTab != GalleryDetailTab.INFO,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            IconButton(
                                onClick = { processIntent(GalleryDetailIntent.Share.Image) },
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(Res.drawable.ic_share),
                                    contentDescription = Localization.string("action_share_image"),
                                    tint = LocalContentColor.current,
                                )
                            }
                        }
                    },
                    windowInsets = persistentTopAppBarWindowInsets(),
                )
            },
            content = { paddingValues ->
                val contentModifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)

                if (state.content != null) {
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
 * Shows current-item actions and available output/source/info tabs.
 *
 * @param state Current item flags, report availability and selected detail tab.
 * @param modifier Layout modifiers for the persistent detail action area.
 * @param processIntent Requests report, form reuse, export, mutation or tab changes.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailNavigationBar(
    state: GalleryDetailState,
    modifier: Modifier = Modifier,
    processIntent: (GalleryDetailIntent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.content?.let { content ->
            if (content.showReportButton) {
                GalleryDetailActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            imageVector = Icons.Default.Report,
                            contentDescription = Localization.string("report_title"),
                        )
                    },
                    label = Localization.string("gallery_action_report"),
                    onClick = { processIntent(GalleryDetailIntent.Report) },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            imageVector = if (content.liked) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = Localization.string(
                                if (content.liked) "gallery_action_unlike" else "gallery_action_like",
                            ),
                        )
                    },
                    label = Localization.string(
                        if (content.liked) "gallery_action_unlike" else "gallery_action_like",
                    ),
                    onClick = { processIntent(GalleryDetailIntent.ToggleLike) },
                )
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            imageVector = Icons.Default.Edit,
                            contentDescription = Localization.string("action_send_to_txt2img"),
                        )
                    },
                    label = Localization.string("home_tab_txt_to_img"),
                    onClick = { processIntent(GalleryDetailIntent.SendTo.Txt2Img) },
                )
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            imageVector = Icons.Default.Image,
                            contentDescription = Localization.string("action_send_to_img2img"),
                        )
                    },
                    label = Localization.string("home_tab_img_to_img"),
                    onClick = { processIntent(GalleryDetailIntent.SendTo.Img2Img) },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            imageVector = if (content.hidden) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = Localization.string("gallery_action_blur"),
                        )
                    },
                    label = Localization.string("gallery_action_blur"),
                    onClick = { processIntent(GalleryDetailIntent.ToggleVisibility) },
                )
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            imageVector = Icons.Default.Share,
                            contentDescription = Localization.string("action_share_prompt"),
                        )
                    },
                    label = Localization.string("hint_prompt"),
                    onClick = { processIntent(GalleryDetailIntent.Share.Params) },
                )
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            imageVector = Icons.Default.Delete,
                            contentDescription = Localization.string("action_delete_image"),
                        )
                    },
                    label = Localization.string("gallery_action_delete"),
                    onClick = { processIntent(GalleryDetailIntent.Delete.Request) },
                )
            }
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = persistentBottomBarWindowInsets(),
            ) {
                state.tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = state.selectedTab == tab,
                        label = {
                            Text(
                                text = tab.label,
                                color = LocalContentColor.current,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors().copy(
                            selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                        ),
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                            )
                        },
                        onClick = { processIntent(GalleryDetailIntent.SelectTab(tab)) },
                    )
                }
            }
        }
    }
}

/**
 * Renders one compact labeled detail action without owning its behavior.
 *
 * @param modifier Size and placement of the action button.
 * @param icon Action icon rendered before the label.
 * @param label Localized action name.
 * @param onClick Requests the corresponding operation from the caller.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailActionButton(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier.height(42.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        onClick = onClick,
    ) {
        icon()
        Text(
            modifier = Modifier.padding(start = 6.dp),
            text = label,
            color = LocalContentColor.current,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
 *
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
 *
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
