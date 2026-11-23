@file:OptIn(ExperimentalFoundationApi::class)

package com.shifthackz.aisdv1.presentation.gallery.list

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalFloatingNavigationBarInset
import com.shifthackz.aisdv1.presentation.components.widget.work.BackgroundWorkWidget
import kotlin.random.Random

/**
 * Renders stable gallery identities with bounded metadata/pixel loading and reports viewport movement.
 *
 * @param modifier Layout modifiers for the gallery scaffold.
 * @param state Gallery ordering, current metadata window, selection and dialogs.
 * @param imageLoader Shared revision-aware sampled image loader; null supports static preview placeholders.
 * @param processIntent Requests viewport metadata, selection/mutations, export or navigation.
 * @param listState Retained grid position used for scrolling and viewport reporting.
 * @param backgroundWorkContent Background-generation status supplied by the application host.
 * @param imageContent Optional cell-pixel renderer receiving metadata and the current image border color.
 * @author Dmitriy Moroz
 */
@Composable
fun GalleryScreenContent(
    modifier: Modifier = Modifier,
    state: GalleryState,
    imageLoader: GalleryImageLoader? = null,
    processIntent: (GalleryIntent) -> Unit = {},
    listState: LazyGridState = rememberLazyGridState(),
    backgroundWorkContent: @Composable () -> Unit = {
        BackgroundWorkWidget(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                .padding(vertical = 4.dp),
        )
    },
    imageContent: (@Composable (GalleryGridItemUi, Color) -> Unit)? = null,
) {
    val metadata = remember(state.items) { state.items.associateBy(GalleryGridItemUi::id) }
    if (!LocalInspectionMode.current) {
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }.distinctUntilChanged()
                .collect { processIntent(GalleryIntent.Viewport(it)) }
        }
    }

    Box(modifier) {
        PlatformScaffold(
            canUnderlapToolbar = true,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column {
                    PlatformTopAppBar(
                        navigationIcon = {
                            AnimatedContent(
                                targetState = state.selectionMode,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "main_nav_icon_animation",
                            ) { isInSelectionMode ->
                                ToolbarIconButton(
                                    onClick = {
                                        val intent = if (isInSelectionMode) {
                                            GalleryIntent.ChangeSelectionMode(false)
                                        } else {
                                            GalleryIntent.OpenDrawer
                                        }
                                        processIntent(intent)
                                    },
                                ) {
                                    Icon(
                                        imageVector = if (isInSelectionMode) {
                                            Icons.Default.Close
                                        } else {
                                            Icons.Default.Menu
                                        },
                                        contentDescription = null,
                                    )
                                }
                            }
                        },
                        title = {
                            Text(
                                text = Localization.string("title_gallery"),
                                style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                            )
                        },
                        actions = {
                            AnimatedContent(
                                targetState = state.selectionMode,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "action_nav_icon_animation",
                            ) { isInSelectionMode ->
                                if (!isInSelectionMode) {
                                    AnimatedVisibility(
                                        visible = state.itemIds.isNotEmpty(),
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                    ) {
                                        ToolbarIconButton(
                                            onClick = {
                                                processIntent(GalleryIntent.Dropdown.Toggle)
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = state.dropdownMenuShow,
                                onDismissRequest = { processIntent(GalleryIntent.Dropdown.Close) },
                                containerColor = MaterialTheme.colorScheme.background,
                            ) {
                                ContextualHelpMenuItem(
                                    title = Localization.string("gallery_menu_selection_mode"),
                                    message = Localization.string("help_gallery_selection"),
                                    icon = Icons.Default.Checklist,
                                    onClick = {
                                        processIntent(GalleryIntent.Dropdown.Close)
                                        processIntent(GalleryIntent.ChangeSelectionMode(true))
                                    },
                                )
                                if (state.mediaStoreInfo.isNotEmpty) {
                                    ContextualHelpMenuItem(
                                        title = Localization.string("browse"),
                                        message = Localization.string("help_gallery_browse"),
                                        icon = Icons.Default.FileOpen,
                                        onClick = {
                                            processIntent(GalleryIntent.Dropdown.Close)
                                            processIntent(GalleryIntent.OpenMediaStoreFolder)
                                        },
                                    )
                                }
                                ContextualHelpMenuItem(
                                    title = Localization.string("gallery_menu_export_all"),
                                    message = Localization.string("help_gallery_export_all"),
                                    icon = Icons.Default.Share,
                                    onClick = {
                                        processIntent(GalleryIntent.Dropdown.Close)
                                        processIntent(GalleryIntent.Export.All.Request)
                                    },
                                )
                                ContextualHelpMenuItem(
                                    title = Localization.string("gallery_menu_delete_all"),
                                    message = Localization.string("help_gallery_delete_all"),
                                    icon = Icons.Default.Delete,
                                    onClick = {
                                        processIntent(GalleryIntent.Dropdown.Close)
                                        processIntent(GalleryIntent.Delete.All.Request)
                                    },
                                )
                            }
                        },
                        windowInsets = WindowInsets(0, 0, 0, 0),
                    )
                    backgroundWorkContent()
                }
            },
            bottomBar = {
                GalleryBottomBars(
                    state = state,
                    processIntent = processIntent,
                )
            },
        ) { paddingValues ->
            ToolbarScrollObserver(!state.loading && state.itemIds.isNotEmpty() && listState.canScrollBackward)
            when {
                state.loading -> GalleryLoadingGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                    grid = state.grid,
                )

                state.itemIds.isEmpty() -> GalleryEmptyState(Modifier.fillMaxSize())
                else -> LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                        .verticalScrollbar(listState, topInset = LocalToolbarContentInset.current),
                    columns = GridCells.Fixed(state.grid.size),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp + LocalToolbarContentInset.current,
                        end = 16.dp,
                        bottom = 16.dp + if (LocalFloatingNavigationBarInset.current > 0.dp) {
                            maxOf(
                                LocalFloatingNavigationBarInset.current,
                                paddingValues.calculateBottomPadding()
                            )
                        } else 0.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = listState,
                ) {
                    items(
                        items = state.itemIds,
                        key = { it },
                    ) { id ->
                        val item = metadata[id]
                        if (item == null) {
                            GalleryUiItemShimmer(); return@items
                        }
                        val selected = state.selection.contains(item.id)
                        GalleryUiItem(
                            modifier = Modifier
                                .selectionJiggle(
                                    enabled = state.selectionMode && !selected,
                                    animationStartOffset = remember(item.id) {
                                        Random.nextInt(0, 320)
                                    },
                                ),
                            item = item,
                            imageLoader = imageLoader,
                            imageContent = imageContent?.let { content ->
                                { borderColor -> content(item, borderColor) }
                            },
                            selectionMode = state.selectionMode,
                            checked = selected,
                            onCheckedChange = {
                                processIntent(GalleryIntent.ToggleItemSelection(item.id))
                            },
                            onLongClick = {
                                processIntent(GalleryIntent.ChangeSelectionMode(true))
                                processIntent(GalleryIntent.ToggleItemSelection(item.id))
                            },
                            onClick = {
                                processIntent(GalleryIntent.OpenItem(it.id))
                            },
                        )
                    }
                    items(2, key = { "bottom_spacer_$it" }) {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
        GalleryDialogRenderer(
            dialog = state.dialog,
            processIntent = processIntent,
        )
    }
}
