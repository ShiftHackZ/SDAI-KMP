@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.history

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSection
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSubtree
import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar

/**
 * Localized labels and metadata formatting used by the history renderer.
 *
 * @param title Screen heading.
 * @param empty Message displayed before any history entries exist.
 * @param refresh Accessible label for reloading history after an error.
 * @param imageUnavailable Description for an absent or undecodable preview.
 * @param resultMeta Formats dimensions and only the sampling fields applicable to the entry type.
 *
 * @author Dmitriy Moroz
 */
data class HistoryStrings(
    val title: String = Localization.string("title_gallery"),
    val empty: String = Localization.string("gallery_empty_title"),
    val refresh: String = Localization.string("action_update"),
    val imageUnavailable: String = Localization.string("message_image_unavailable"),
    val resultMeta: (GalleryItemMetadata) -> String = { item ->
        if (item.type == AiGenerationResult.Type.UPSCALE) "${item.width} X ${item.height}"
        else Localization.string(
            "history_result_meta",
            item.width,
            item.height,
            item.samplingSteps,
            item.cfgScale,
        )
    },
)

/**
 * Renders generation metadata with sampled image previews and explicit reload/error states.
 *
 * @param state Current history entries and loading/error status.
 * @param processIntent Requests back navigation or a fresh persistent-history read.
 * @param modifier Layout modifiers for the history scaffold.
 * @param imageLoader Revision-aware sampled loader; absent loaders render preview placeholders.
 * @param strings Localized labels and generation-metadata formatter.
 * @param imageContent Thumbnail renderer receiving the entry and its weighted, aspect-ratio-constrained modifier.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun HistoryScreenContent(
    state: HistoryState,
    processIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier,
    imageLoader: GalleryImageLoader? = null,
    strings: HistoryStrings = HistoryStrings(),
    imageContent: @Composable (GalleryItemMetadata, Modifier) -> Unit = { item, imageModifier ->
        HistoryThumbnail(
            item = item,
            imageUnavailable = strings.imageUnavailable,
            modifier = imageModifier,
            imageLoader = imageLoader,
        )
    },
) {
    PlatformScaffold(
        canUnderlapToolbar = true,
        modifier = modifier.fillMaxSize(),
        topBar = {
            PlatformTopAppBar(
                navigationIcon = {
                    ToolbarBackButton(
                        onClick = { processIntent(HistoryIntent.NavigateBack) },
                        icon = Icons.AutoMirrored.Filled.ArrowBack
                    )
                },
                title = {
                    Text(
                        text = strings.title,
                        style = platformToolbarTitleStyle(MaterialTheme.typography.titleLarge),
                    )
                },
                actions = {
                    ToolbarIconButton(onClick = { processIntent(HistoryIntent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                        )
                    }
                },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.loading -> CircularProgressIndicator(modifier = Modifier.size(48.dp))
                state.error != null -> HistoryError(
                    message = state.error,
                    strings = strings,
                    processIntent = processIntent,
                )

                state.items.isEmpty() -> Text(
                    modifier = Modifier.padding(24.dp),
                    text = strings.empty,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> {
                    val listState = rememberLazyListState()
                    ToolbarScrollObserver(listState.canScrollBackward)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScrollbar(
                                listState,
                                topInset = LocalToolbarContentInset.current
                            ),
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp + LocalToolbarContentInset.current,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = state.items,
                            key = GalleryItemMetadata::id,
                        ) { item ->
                            HistoryItem(
                                item = item,
                                strings = strings,
                                imageContent = imageContent,
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * Shows a persistent-history read failure and an explicit retry action.
 *
 * @param message User-visible failure returned by the history reader.
 * @param strings Localized refresh label.
 * @param processIntent Receives the refresh request.
 * @param modifier Placement of the error and retry group.
 * @author Dmitriy Moroz
 */
@Composable
private fun HistoryError(
    message: String,
    strings: HistoryStrings,
    processIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedButton(onClick = { processIntent(HistoryIntent.Refresh) }) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
            )
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = strings.refresh,
            )
        }
    }
}


/**
 * Pairs a caller-rendered thumbnail with truncated prompt text and generation parameters.
 *
 * @param item Metadata determining text and thumbnail aspect ratio.
 * @param strings Localized generation-parameter formatter.
 * @param modifier Placement of the history card.
 * @param imageContent Thumbnail renderer receiving the card-controlled image geometry.
 * @author Dmitriy Moroz
 */
@Composable
private fun HistoryItem(
    item: GalleryItemMetadata,
    strings: HistoryStrings,
    modifier: Modifier = Modifier,
    imageContent: @Composable (GalleryItemMetadata, Modifier) -> Unit,
) {
    UiDiagnosticSection(UiDiagnosticSubtree.HISTORY_IMAGE) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                imageContent(item, Modifier.weight(0.38f).aspectRatio(item.aspectRatio))
                Column(
                    modifier = Modifier.weight(0.62f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.prompt.ifBlank { item.type.key },
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = strings.resultMeta(item),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
