package com.shifthackz.aisdv1.presentation.configuration.upscaler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceEmptyState
import com.shifthackz.aisdv1.presentation.configuration.setup.source.isItemFullyVisible
import kotlinx.coroutines.launch

/**
 * Presents discovered upscalers with generation-picker spacing, toolbar underlap and selection scrolling.
 *
 * @param state Platform-allowed choices and current discovery draft.
 * @param strings Localized metadata and no-results labels.
 * @param processIntent Publishes provider selection without committing saved configuration.
 * @param modifier Bounds supplied by the wizard pager.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerSourceSelectionStep(
    state: UpscalerSetupState, strings: ServerSetupStrings,
    processIntent: (UpscalerSetupIntent) -> Unit, modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val sources = state.visibleSources(strings)
    ToolbarScrollObserver(listState.canScrollBackward)
    LazyColumn(
        modifier.verticalScrollbar(listState, topInset = LocalToolbarContentInset.current),
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp, top = 16.dp + LocalToolbarContentInset.current,
            end = 16.dp, bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (sources.isEmpty()) item("source_empty") { SourceEmptyState(strings.sourceNoResults) }
        itemsIndexed(sources, key = { _, source -> source.key }) { index, source ->
            UpscalerSourceItem(
                source, state.platform, source == state.source, strings,
                onClick = {
                    scope.launch {
                        if (!listState.isItemFullyVisible(index)) listState.animateScrollToItem(
                            index
                        )
                    }
                    processIntent(UpscalerSetupIntent.SelectSource(source))
                }
            )
        }
    }
}
