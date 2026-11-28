package com.shifthackz.aisdv1.presentation.benchmark

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialog
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle

/**
 * Presents device capabilities, provider recommendations and progress with run/share actions.
 *
 * @param modifier Placement of the complete benchmark scaffold.
 * @param state Device information, measured results, progress and any benchmark failure.
 * @param processIntent Requests running, sharing, error dismissal or back navigation.
 * @param listState Caller-owned scroll position for inspecting each benchmark section.
 * @author Dmitriy Moroz
 */
@Composable
fun BenchmarkScreenContent(
    state: BenchmarkState,
    modifier: Modifier = Modifier,
    processIntent: (BenchmarkIntent) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
) {
    PlatformScaffold(
        canUnderlapToolbar = true,
        modifier = modifier,
        topBar = {
            PlatformTopAppBar(
                navigationIcon = {
                    ToolbarBackButton(
                        onClick = { processIntent(BenchmarkIntent.NavigateBack) },
                        icon = Icons.AutoMirrored.Outlined.ArrowBack
                    )
                },
                title = {
                    Text(
                        text = Localization.string("title_benchmark"),
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                    )
                },
                actions = {
                    ToolbarIconButton(
                        isEnabled = state.latestResult != null && !state.running,
                        onClick = { processIntent(BenchmarkIntent.ShareResults) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = Localization.string("benchmark_share_results"),
                        )
                    }
                },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
        bottomBar = {
            PrimaryButton(
                modifier = Modifier
                    .windowInsetsPadding(persistentBottomBarWindowInsets())
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp, top = 8.dp),
                isEnabled = !state.running,
                onClick = { processIntent(BenchmarkIntent.RunBenchmark) },
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = if (state.running) {
                        Localization.string("benchmark_running")
                    } else {
                        Localization.string("benchmark_run")
                    },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            ToolbarScrollObserver(listState.canScrollBackward)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(listState, topInset = LocalToolbarContentInset.current),
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp + LocalToolbarContentInset.current,
                    end = 16.dp,
                    bottom = 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ScoreSection(
                        result = state.latestResult,
                        isLoading = state.loadingResult,
                        isRunning = state.running,
                    )
                }
                item {
                    DeviceSection(
                        isLoading = state.loadingDevice,
                        deviceInfo = state.deviceInfo ?: state.latestResult?.deviceInfo,
                    )
                }
                item {
                    CapabilitiesSection(
                        isLoading = state.loadingDevice,
                        deviceInfo = state.deviceInfo ?: state.latestResult?.deviceInfo,
                    )
                }
                item {
                    ProviderRecommendationsSection(
                        isLoading = state.loadingResult,
                        deviceInfo = state.deviceInfo ?: state.latestResult?.deviceInfo,
                        recommendations = state.latestResult?.providerRecommendations.orEmpty(),
                    )
                }
            }
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                visible = state.error != null,
            ) {
                state.error?.let { error ->
                    BenchmarkErrorCard(
                        message = error,
                        onDismiss = { processIntent(BenchmarkIntent.DismissError) },
                    )
                }
            }
        }
    }
    if (state.running) {
        ProgressDialog(
            title = Localization.string("benchmark_running_title").asUiText(),
            subTitle = Localization.string("benchmark_running_sub_title").asUiText(),
            canDismiss = false,
        )
    }
}
