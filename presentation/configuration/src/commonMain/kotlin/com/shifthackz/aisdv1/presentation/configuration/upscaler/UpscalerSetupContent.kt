@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.configuration.upscaler

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import com.shifthackz.aisdv1.presentation.components.widget.sheet.PlatformModalBottomSheet
import com.shifthackz.aisdv1.presentation.components.widget.input.SearchFilterToolbarHeight
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.isPlatformToolbarNative
import com.shifthackz.aisdv1.presentation.configuration.setup.source.ProviderDiscoveryToolbar
import com.shifthackz.aisdv1.presentation.configuration.setup.source.ProviderDiscoveryToolbarReveal
import com.shifthackz.aisdv1.presentation.configuration.setup.source.ProviderSortBottomSheet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.shifthackz.aisdv1.presentation.components.widget.action.TextActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.presentation.components.model.description
import com.shifthackz.aisdv1.presentation.components.model.displayName
import com.shifthackz.aisdv1.presentation.configuration.setup.form.remote.FormTitle
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryActionButton
import com.shifthackz.aisdv1.presentation.components.widget.action.ScreenActionBar
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformBackHandler
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalBackTransitionState
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationInputEnabled
import com.shifthackz.aisdv1.presentation.components.widget.navigation.rememberBackTransitionState
import com.shifthackz.aisdv1.presentation.components.widget.navigation.ProgrammaticPager
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus
import com.shifthackz.aisdv1.presentation.configuration.setup.form.local.LocalCatalogStatusCard
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ConfigurationWizardScaffold
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.form.local.LocalModelItem

/**
 * Reuses the generation wizard's scaffold, step strip, pager and downloaded-model rows.
 * iOS Back previews the provider step and commits only after the local transition completes.
 *
 * @param state Dedicated configuration draft and transfer snapshots.
 * @param processIntent Dispatches configuration and transfer actions.
 * @param modifier Screen placement supplied by the route host.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerSetupContent(
    state: UpscalerSetupState, processIntent: (UpscalerSetupIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val localBack = rememberBackTransitionState()
    val routeBack = LocalBackTransitionState.current
    val currentState by rememberUpdatedState(state)
    val currentIntent by rememberUpdatedState(processIntent)
    val isConfiguration = state.step == ServerSetupState.Step.CONFIGURE
    val shouldAnimateStepBack = isConfiguration && isPlatformToolbarNative
    val canStartGesture = state.error == null && state.pendingDeleteId == null &&
            !state.discovery.isFilterVisible && !state.discovery.isSortVisible
    val currentCanStartGesture by rememberUpdatedState(canStartGesture)
    LaunchedEffect(state.step) {
        if (!isConfiguration) {
            withFrameNanos { }
            localBack.reset()
        }
    }
    val onBack = {
        if (shouldAnimateStepBack) {
            localBack.complete(
                canCommit = {
                    currentState.step == ServerSetupState.Step.CONFIGURE && currentCanStartGesture
                },
                onBack = { currentIntent(UpscalerSetupIntent.Back) },
            )
        } else currentIntent(UpscalerSetupIntent.Back)
    }
    val strings = ServerSetupStrings(title = Localization.string("upscaler_configuration"))
    val fraction by animateFloatAsState(
        targetValue = if (state.step == ServerSetupState.Step.SOURCE) 1f else 0f,
        animationSpec = tween(240), label = "upscaler-search-reveal",
    )
    val discovery = state.discovery
    val onDiscover: (UpscalerDiscoveryState) -> Unit =
        { processIntent(UpscalerSetupIntent.Discover(it)) }
    val toolbar: @Composable () -> Unit = {
        ProviderDiscoveryToolbarReveal(fraction) {
            ProviderDiscoveryToolbar(
                query = discovery.query,
                hasActiveFilters = discovery.type != null ||
                        discovery.readiness.isNotEmpty() || discovery.tags.isNotEmpty(),
                strings = strings, onQueryChange = { onDiscover(discovery.copy(query = it)) },
                onOpenFilters = { onDiscover(discovery.copy(isFilterVisible = true)) },
                onOpenSort = { onDiscover(discovery.copy(isSortVisible = true)) },
            )
        }
    }
    if (!LocalInspectionMode.current) {
        PlatformBackHandler(
            isEnabled = true,
            onBack = { currentIntent(UpscalerSetupIntent.Back) },
            canStartGesture = canStartGesture && (!isConfiguration || shouldAnimateStepBack),
            transitionState = if (isConfiguration) localBack else routeBack,
        )
    }
    ConfigurationWizardScaffold(
        title = strings.title,
        step = state.step,
        strings = strings,
        canNavigateBack = true,
        onBack = onBack,
        modifier = modifier,
        toolbarExtensionHeight = if (isPlatformToolbarNative) SearchFilterToolbarHeight * fraction else 0.dp,
        toolbarExtension = { if (isPlatformToolbarNative) toolbar() },
        afterToolbar = { if (!isPlatformToolbarNative) toolbar() },
        actions = {
            ScreenActionBar {
                PrimaryActionButton(
                    isEnabled = state.isSupported && !state.isLoading &&
                            (state.step == ServerSetupState.Step.SOURCE || state.models.any {
                                it.id == state.selectedId && it.downloaded && UpscalerSource.forModel(
                                    it.id
                                ) == state.source
                            }),
                    onClick = { processIntent(if (state.step == ServerSetupState.Step.SOURCE) UpscalerSetupIntent.Next else UpscalerSetupIntent.Save) }) {
                    Text(if (state.step == ServerSetupState.Step.SOURCE) strings.next else strings.setup)
                }
            }
        }) { padding ->
        ProgrammaticPager(
            selectedPage = state.step.ordinal, pageCount = 2,
            modifier = Modifier.padding(padding).fillMaxSize(),
            shouldAnimate = !LocalInspectionMode.current && !localBack.hasCommittedGesture,
            backProgress = if (shouldAnimateStepBack && localBack.isActive) localBack.progress else null,
        ) { page ->
            val canSendIntent = LocalNavigationInputEnabled.current && page == state.step.ordinal
            val pageIntent: (UpscalerSetupIntent) -> Unit = { if (canSendIntent) currentIntent(it) }
            if (page == 0) {
                UpscalerSourceSelectionStep(
                    state,
                    strings,
                    pageIntent,
                    Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(
                        top = 24.dp + LocalToolbarContentInset.current,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column {
                            FormTitle(
                                title = state.source.displayName,
                                subtitle = state.source.description,
                            )
                        }
                    }
                    if (!state.isSupported) {
                        item { Text(Localization.string("upscaler_unavailable")) }
                    } else {
                        if (state.isLoading && state.catalogStatus == LocalCatalogStatus.LOADING) item { CircularProgressIndicator() }
                        items(
                            state.models.filter { UpscalerSource.forModel(it.id) == state.source },
                            key = { it.id }) { model ->
                            val transfer = state.downloads.lastOrNull { it.modelId == model.id }
                            LocalModelItem(
                                model = ServerSetupState.LocalModel(
                                    model.id,
                                    model.name,
                                    model.size,
                                    model.sources,
                                    model.downloaded,
                                    transfer?.progress ?: DownloadState.Unknown
                                ),
                                selected = model.id == state.selectedId, strings = strings,
                                onSelect = { pageIntent(UpscalerSetupIntent.Select(model.id)) },
                                onAction = { pageIntent(UpscalerSetupIntent.ModelAction(model.id)) })
                        }
                        if (state.catalogStatus == LocalCatalogStatus.ERROR ||
                            (!state.isLoading && state.models.none { UpscalerSource.forModel(it.id) == state.source })
                        ) {
                            item {
                                LocalCatalogStatusCard(
                                    state.catalogStatus, state.isLoading,
                                    onRetry = { pageIntent(UpscalerSetupIntent.Retry) })
                            }
                        }
                    }
                }
            }
        }
    }
    if (discovery.isFilterVisible) {
        PlatformModalBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { it != SheetValue.PartiallyExpanded }),
            shape = RectangleShape, containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { onDiscover(discovery.copy(isFilterVisible = false)) },
        ) { UpscalerFiltersBottomSheet(state, strings, onDiscover) }
    }
    if (discovery.isSortVisible) {
        PlatformModalBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { it != SheetValue.PartiallyExpanded }),
            shape = RectangleShape, containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { onDiscover(discovery.copy(isSortVisible = false)) },
        ) {
            ProviderSortBottomSheet(discovery.sortOrder, strings, onSelect = {
                onDiscover(discovery.copy(sortOrder = it, isSortVisible = false))
            })
        }
    }
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { processIntent(UpscalerSetupIntent.Dismiss) },
            title = { Text(strings.errorTitle) }, text = { Text(state.error) },
            confirmButton = {
                TextActionButton(onClick = { processIntent(UpscalerSetupIntent.Retry) }) {
                    Text(
                        Localization.string("upscaler_retry")
                    )
                }
            },
            dismissButton = {
                TextActionButton(onClick = { processIntent(UpscalerSetupIntent.Dismiss) }) {
                    Text(
                        strings.cancel
                    )
                }
            }
        )
    }
    if (state.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { processIntent(UpscalerSetupIntent.Dismiss) },
            title = { Text(strings.deleteLocalModelTitle) },
            confirmButton = {
                TextActionButton(onClick = { processIntent(UpscalerSetupIntent.ConfirmDelete) }) {
                    Text(
                        strings.ok
                    )
                }
            },
            dismissButton = {
                TextActionButton(onClick = { processIntent(UpscalerSetupIntent.Dismiss) }) {
                    Text(
                        strings.cancel
                    )
                }
            }
        )
    }
}
