@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.logger

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import kotlinx.coroutines.launch

/**
 * Localized toolbar and empty-state labels for the stateless log viewer.
 *
 * @param title Log-viewer heading.
 * @param emptyMessage Message displayed when the current snapshot contains no text.
 * @param backContentDescription Accessible back-navigation label.
 * @param refreshContentDescription Accessible label for reading a fresh snapshot.
 * @param copyContentDescription Accessible label for copying displayed text.
 * @param shareContentDescription Accessible label for native text sharing.
 * @param scrollTopContentDescription Accessible label for scrolling to the beginning.
 * @param scrollBottomContentDescription Accessible label for scrolling to the latest end.
 *
 * @author Dmitriy Moroz
 */
data class LoggerScreenStrings(
    val title: String = Localization.string("title_debug_logger"),
    val emptyMessage: String = Localization.string("debug_logger_empty"),
    val backContentDescription: String = Localization.string("action_back"),
    val refreshContentDescription: String = Localization.string("action_refresh"),
    val copyContentDescription: String = Localization.string("action_copy"),
    val shareContentDescription: String = Localization.string("action_share_prompt"),
    val scrollTopContentDescription: String = Localization.string("action_scroll_to_top"),
    val scrollBottomContentDescription: String = Localization.string("action_scroll_to_bottom"),
)

/**
 * File-read status and text rendered by the stateless log viewer.
 *
 * @param loading Whether a new log snapshot is loading.
 * @param text Current log snapshot, empty before a successful read.
 *
 * @author Dmitriy Moroz
 */
data class LoggerScreenContentState(
    val loading: Boolean = true,
    val text: String = "",
)

/**
 * Reload, native-action and navigation requests emitted by the log renderer.
 *
 * @author Dmitriy Moroz
 */
sealed interface LoggerScreenAction {

    data object ReadLogs : LoggerScreenAction

    data object CopyLogs : LoggerScreenAction

    data object ShareLogs : LoggerScreenAction

    data object NavigateBack : LoggerScreenAction
}

/**
 * Maps MVI log state to the stateless renderer without copying log content.
 *
 * @author Dmitriy Moroz
 */
fun LoggerState.toContentState() = LoggerScreenContentState(
    loading = loading,
    text = text,
)

/**
 * Maps renderer actions to ViewModel requests for reload, native operations and navigation.
 *
 * @author Dmitriy Moroz
 */
fun LoggerScreenAction.toIntent(): LoggerIntent = when (this) {
    LoggerScreenAction.ReadLogs -> LoggerIntent.ReadLogs
    LoggerScreenAction.CopyLogs -> LoggerIntent.CopyLogs
    LoggerScreenAction.ShareLogs -> LoggerIntent.ShareLogs
    LoggerScreenAction.NavigateBack -> LoggerIntent.NavigateBack
}

/**
 * Shows selectable log text with refresh, copy/share and scroll controls.
 *
 * @param strings Localized toolbar and empty-state labels.
 * @param state Current file-read status and log text.
 * @param processAction Requests reload, copy/share or back navigation.
 * @param modifier Layout modifiers for the log-viewer scaffold.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun LoggerScreenContent(
    strings: LoggerScreenStrings,
    state: LoggerScreenContentState,
    processAction: (LoggerScreenAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    PlatformScaffold(
        canUnderlapToolbar = true,
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PlatformTopAppBar(
                navigationIcon = {
                    ToolbarBackButton(
                        onClick = { processAction(LoggerScreenAction.NavigateBack) },
                        icon = Icons.AutoMirrored.Outlined.ArrowBack
                    )
                },
                title = {
                    Text(
                        text = strings.title,
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                    )
                },
                actions = {
                    AnimatedVisibility(
                        visible = !state.loading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Row {
                            AnimatedVisibility(
                                visible = state.text.isNotBlank(),
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Row {
                                    ToolbarIconButton(
                                        onClick = { processAction(LoggerScreenAction.CopyLogs) },
                                        content = {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = strings.copyContentDescription,
                                            )
                                        },
                                    )
                                    ToolbarIconButton(
                                        onClick = { processAction(LoggerScreenAction.ShareLogs) },
                                        content = {
                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = strings.shareContentDescription,
                                            )
                                        },
                                    )
                                }
                            }
                            ToolbarIconButton(
                                onClick = { processAction(LoggerScreenAction.ReadLogs) },
                                content = {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = strings.refreshContentDescription,
                                    )
                                },
                            )
                        }
                    }
                },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !state.loading && state.text.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Row(
                    modifier = Modifier
                        .windowInsetsPadding(persistentBottomBarWindowInsets())
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        },
                        content = {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = strings.scrollTopContentDescription,
                            )
                        },
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        },
                        content = {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = strings.scrollBottomContentDescription,
                            )
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        ToolbarScrollObserver(scrollState.canScrollBackward)
        val text = if (!state.loading) state.text else ""
        val scrollStateHorizontal = rememberScrollState()
        if (!state.loading && state.text.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = strings.emptyMessage,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScrollbar(scrollState, topInset = LocalToolbarContentInset.current)
                .verticalScroll(scrollState)
                .padding(top = LocalToolbarContentInset.current),
        ) {
            AnimatedVisibility(
                visible = state.loading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(60.dp)
                            .aspectRatio(1f),
                    )
                }
            }
            Text(
                modifier = Modifier.horizontalScroll(scrollStateHorizontal),
                text = text,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 12.sp,
            )
        }
        LaunchedEffect(state.text) {
            if (!state.loading) {
                scrollState.scrollTo(scrollState.maxValue)
            }
        }
    }
}
