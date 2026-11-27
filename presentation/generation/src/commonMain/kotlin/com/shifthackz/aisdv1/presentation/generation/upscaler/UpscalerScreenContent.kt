package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.widget.dialog.DecisionInteractiveDialog
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.GenerationActionBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarNavigationButton
import com.shifthackz.aisdv1.presentation.generation.modal.GenerationModalRenderer

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

/**
 * Renders standalone upscaler inputs, pinned actions and operation dialogs from retained state.
 *
 * @param state Captured input, provider selection and operation feedback.
 * @param upscalerFormState Dedicated model catalog and configuration status.
 * @param processIntent Routes user actions to the screen owner.
 * @param modifier Bounds supplied by the Home route host.
 * @param listState Scroll position retained by the caller or this composition.
 * @param performanceOverlay Preference-controlled resource panel inside operation dialogs.
 * @param inputImageContent Image acquisition card using the same retained screen inputs.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerScreenContent(
    state: UpscalerState,
    upscalerFormState: UpscalerFormState,
    processIntent: (UpscalerIntent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    performanceOverlay: @Composable () -> Unit = {},
    inputImageContent: @Composable () -> Unit = { UpscalerImageInput(state, processIntent) },
) {
    PlatformScaffold(modifier = modifier, topBar = {
        PlatformTopAppBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = { Text(Localization.string("upscaler_title")) },
            navigationIcon = {
                ToolbarNavigationButton(
                    canNavigateBack = false,
                    onClick = { processIntent(UpscalerIntent.OpenDrawer) })
            })
    }, bottomBar = {
        GenerationActionBar {
            PrimaryButton(
                modifier = Modifier.fillMaxWidth().height(60.dp)
                    .padding(horizontal = 16.dp).padding(bottom = 16.dp),
                isEnabled = !state.isRunning && state.image.isNotEmpty() &&
                        !state.configuration?.modelId.isNullOrBlank() && !state.isPickingImage,
                onClick = { processIntent(UpscalerIntent.Run) },
            ) {
                Icon(Icons.Default.PhotoSizeSelectLarge, contentDescription = null)
                Text(
                    Localization.string("upscaler_action"),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            state = listState,
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                UpscalerProviderCard(
                    platform = state.platform,
                    source = state.configuration?.source
                        ?: upscalerFormState.model?.let { UpscalerSource.forModel(it.id) }
                        ?: UpscalerSource.availableOn(state.platform).firstOrNull(),
                    onConfigure = { processIntent(UpscalerIntent.Configure) },
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            item {
                inputImageContent()
            }
            item {
                UpscalerForm(
                    state = upscalerFormState,
                    configuration = state.configuration,
                    outputSize = state.outputSize,
                    onConfigurationChange = { processIntent(UpscalerIntent.ConfigureModel(it)) },
                    onConfigure = { processIntent(UpscalerIntent.Configure) },
                    isEnabled = !state.isRunning, isStandalone = true,
                )
            }
        }
    }
    if (state.hasDownscaleWarning) {
        DecisionInteractiveDialog(
            title = Localization.string("upscaler_downscale_title").asUiText(),
            text = Localization.string("upscaler_downscale_message").asUiText(),
            confirmActionText = Localization.string("upscaler_continue").asUiText(),
            dismissActionText = Localization.string("cancel").asUiText(),
            onConfirmAction = { processIntent(UpscalerIntent.ConfirmDownscale) },
            onDismissRequest = { processIntent(UpscalerIntent.DismissModal) },
        )
    }
    GenerationModalRenderer(
        screenModal = state.screenModal, generationStartedAt = state.generationStartedAt,
        performanceOverlay = performanceOverlay,
        onDismissRequest = { processIntent(UpscalerIntent.DismissModal) },
        onCancelGeneration = { processIntent(UpscalerIntent.Cancel) },
        onSaveRequest = { processIntent(UpscalerIntent.SaveResults(it)) },
        onReportRequest = { processIntent(UpscalerIntent.ReportResult(it)) },
        onViewDetailRequest = { processIntent(UpscalerIntent.ViewResult(it)) },
        onBenchmarkRequest = {}, onSkipBenchmarkRequest = {},
        onBenchmarkContinueRequest = {}, onBenchmarkDoNotAskRequest = {},
    )
}
