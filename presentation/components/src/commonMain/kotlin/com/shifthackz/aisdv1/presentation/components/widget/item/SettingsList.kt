package com.shifthackz.aisdv1.presentation.components.widget.item

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver

/**
 * Keeps Settings pages aligned with the same horizontal inset, scroll indicator and native toolbar underlap.
 * Each row supplies spacing from [SettingsListDefaults] rather than a second list-wide item gap.
 *
 * @param modifier Placement and external scaffold padding of the list.
 * @param bottomPadding Space occupied by the invoking page's floating navigation, if present.
 * @param state Scroll position, optionally shared with a viewport-based page loader.
 * @param content Keyed Settings headers, rows and supporting content.
 * @author Dmitriy Moroz
 */
@Composable
fun SettingsList(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    state: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit,
) {
    val toolbarInset = LocalToolbarContentInset.current
    ToolbarScrollObserver(state.canScrollBackward)
    LazyColumn(
        modifier = modifier.verticalScrollbar(state, topInset = toolbarInset)
            .padding(horizontal = 16.dp),
        state = state,
        contentPadding = PaddingValues(top = toolbarInset, bottom = bottomPadding),
        content = content,
    )
}
