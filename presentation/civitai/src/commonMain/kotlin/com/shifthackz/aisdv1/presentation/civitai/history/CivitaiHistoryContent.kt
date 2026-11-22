package com.shifthackz.aisdv1.presentation.civitai.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsList
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsListDefaults
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Renders lazy receipt positions while retaining only the metadata around the current viewport.
 *
 * @param state Ordered identities and the bounded loaded receipt page.
 * @param processIntent Viewport, retry and Back requests.
 * @param openUrl Opens a confirmed public post or Civitai account for an unresolved attempt.
 * @param modifier Placement of the full standalone history surface.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CivitaiHistoryContent(
    state: CivitaiHistoryState,
    processIntent: (CivitaiHistoryIntent) -> Unit,
    openUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val metadata = remember(state.uploads) { state.uploads.associateBy { it.id } }
    val itemModifier = Modifier.fillMaxWidth().padding(SettingsListDefaults.itemPadding)
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.distinctUntilChanged()
            .collect { processIntent(CivitaiHistoryIntent.Viewport(it)) }
    }
    PlatformScaffold(
        modifier = modifier.fillMaxSize(),
        canUnderlapToolbar = true,
        topBar = {
            PlatformTopAppBar(
                title = {
                    Text(
                        Localization.string("civitai_history"),
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium)
                    )
                },
                navigationIcon = { ToolbarBackButton(onClick = { processIntent(CivitaiHistoryIntent.Back) }) },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
    ) { padding ->
        if (!state.isLoading && !state.hasFailed && state.itemIds.isEmpty()) {
            CivitaiHistoryEmptyState(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            SettingsList(modifier = Modifier.fillMaxSize().padding(padding), state = listState) {
                if (state.hasFailed) item(key = "failure") {
                    SettingsItem(
                        modifier = itemModifier,
                        startIcon = Icons.Default.Refresh,
                        text = Localization.string("retry").asUiText(),
                        onClick = { processIntent(CivitaiHistoryIntent.Retry) },
                    )
                    Text(
                        Localization.string("error_generic"),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(SettingsListDefaults.supportingTextPadding)
                    )
                }
                if (state.isLoading) items(count = 8, key = { "loading_$it" }) {
                    SettingsItem(
                        modifier = itemModifier,
                        loading = true,
                        text = Localization.string("civitai_history").asUiText()
                    )
                }
                items(count = state.itemIds.size, key = { state.itemIds[it] }) { index ->
                    val upload = metadata[state.itemIds[index]]
                    val label = upload?.url
                        ?: upload?.let { Localization.string("civitai_upload_" + it.status.name.lowercase()) }
                        ?: ""
                    SettingsItem(
                        modifier = itemModifier,
                        loading = upload == null,
                        startIcon = Icons.Default.PhotoLibrary,
                        text = label.asUiText(),
                        onClick = {
                            upload?.let {
                                openUrl(
                                    it.url ?: "https://civitai.com/user/account"
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
