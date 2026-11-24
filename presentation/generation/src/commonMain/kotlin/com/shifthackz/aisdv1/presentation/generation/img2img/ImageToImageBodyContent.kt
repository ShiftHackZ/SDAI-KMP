package com.shifthackz.aisdv1.presentation.generation.img2img

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.withoutVisualEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.platform.ExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionComponent
import com.shifthackz.aisdv1.presentation.components.widget.image.rememberPreviewImage
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.source.CurrentProviderCard
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformLazySurface
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerForm
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerFormState
import com.shifthackz.aisdv1.presentation.generation.widget.imageinput.GenerationImageInput
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormEvent

/**
 * Explains why the selected provider cannot accept image-to-image requests.
 *
 * @param modifier Layout modifiers for the unsupported-provider explanation.
 * @param mode Provider selecting the appropriate explanation.
 * @param strings Localized capability and configuration text.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun UnsupportedImageToImageBody(
    modifier: Modifier = Modifier,
    mode: ServerSource,
    strings: ImageToImageStrings,
) {
    Column(
        modifier = modifier.padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(96.dp),
            imageVector = Icons.Default.DeviceUnknown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.padding(top = 20.dp),
            text = strings.unsupportedTitle,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.padding(top = 14.dp),
            text = when (mode) {
                ServerSource.OPEN_AI -> strings.openAiUnsupportedSubtitle
                ServerSource.OPEN_AI_COMPATIBLE, ServerSource.OPEN_AI_OAUTH -> strings.compatibleUnsupportedSubtitle
                else -> strings.localUnsupportedSubtitle
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (mode != ServerSource.OPEN_AI_COMPATIBLE && mode != ServerSource.OPEN_AI_OAUTH) Text(
            modifier = Modifier.padding(top = 14.dp),
            text = if (mode == ServerSource.OPEN_AI) {
                strings.openAiUnsupportedSubtitle2
            } else {
                strings.localUnsupportedSubtitle2
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Builds the lazy image-to-image input form with a sampled source preview and denoising control.
 *
 * @param state Current provider, source image and generation parameters.
 * @param strings Localized labels and capability explanations.
 * @param promptChipTextFieldState Pending unsubmitted positive-prompt chip text.
 * @param negativePromptChipTextFieldState Pending unsubmitted negative-prompt chip text.
 * @param processIntent Publishes form changes and source-image requests.
 * @param onInPaintClick Opens the shared inpaint editor for the current source.
 * @param modifier Layout modifiers for the lazy input form.
 * @param listState Retained vertical position within the generation form.
 * @param upscalerFormState Independent catalog snapshot shared with the other Home forms.
 * @param isInitiallyExpanded Initial disclosure state of generation options.
 * @param engineContent Checkpoint picker supplied by the owning form.
 * @param inputImageContent Optional source-image card receiving its existing spacing modifier.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImageToImageBody(
    state: ImageToImageState,
    strings: ImageToImageStrings,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    processIntent: (ImageToImageIntent) -> Unit,
    onInPaintClick: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    isInitiallyExpanded: Boolean = true,
    upscalerFormState: UpscalerFormState? = null,
    engineContent: @Composable (Modifier) -> Unit = { EngineSelectionComponent(modifier = it) },
    inputImageContent: (@Composable (Modifier) -> Unit)? = null,
) {
    var isFormExpanded by rememberSaveable { mutableStateOf(isInitiallyExpanded) }
    val overscrollEffect = rememberOverscrollEffect()
    val lastGenerationItemIndex by remember(listState) {
        derivedStateOf { (listState.layoutInfo.totalItemsCount - 2).coerceAtLeast(2) }
    }
    val urlLauncher = if (LocalInspectionMode.current) remember { ExternalUrlLauncher {} }
    else rememberExternalUrlLauncher()
    val onEvent: (GenerationInputFormEvent) -> Unit = remember(processIntent, urlLauncher) {
        { event ->
            if (event == GenerationInputFormEvent.OpenADetailerInstallInstructions) {
                urlLauncher.openUrl("https://github.com/Bing-su/adetailer#install")
            } else event.toImageToImageIntent()?.let(processIntent)
        }
    }
    val denoisingStrength = state.denoisingStrength
    val afterSlidersSection: @Composable () -> Unit = remember(denoisingStrength, processIntent) {
        { DenoisingStrengthSlider(denoisingStrength, processIntent) }
    }
    ToolbarScrollObserver(listState.canScrollBackward)
    PlatformLazySurface(
        state = listState,
        firstItemIndex = 2,
        lastItemIndex = lastGenerationItemIndex,
        modifier = modifier.fillMaxSize(),
        horizontalPadding = 16.dp,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        overscrollEffect = overscrollEffect,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(listState, topInset = LocalToolbarContentInset.current),
            state = listState,
            overscrollEffect = overscrollEffect?.withoutVisualEffect(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp + LocalToolbarContentInset.current,
                bottom = 16.dp
            ),
        ) {
            item(key = "generation-provider") {
                CurrentProviderCard(
                    source = state.mode,
                    platform = state.platform,
                    onClick = { processIntent(ImageToImageIntent.ConfigureProvider) },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            item(key = "generation-input-image") {
                if (inputImageContent != null) inputImageContent(Modifier.padding(bottom = 16.dp))
                else BoxWithConstraints(Modifier.padding(bottom = 16.dp)) {
                    val edge = with(LocalDensity.current) { maxWidth.roundToPx() }
                    ImageInputSection(
                        state = state,
                        strings = strings,
                        image = rememberPreviewImage(state.imageBase64, edge),
                        processIntent = processIntent,
                        onInPaintClick = onInPaintClick,
                    )
                }
            }
            imageToImageFormItems(
                state = state,
                isExpanded = isFormExpanded,
                onToggleExpanded = { isFormExpanded = !isFormExpanded },
                strings = strings,
                promptChipTextFieldState = promptChipTextFieldState,
                negativePromptChipTextFieldState = negativePromptChipTextFieldState,
                onEvent = onEvent,
                afterSlidersSection = afterSlidersSection,
                engineContent = engineContent,
            )
            item(key = "upscaler-form") {
                UpscalerForm(
                    state = upscalerFormState,
                    configuration = state.upscaler,
                    onConfigurationChange = { processIntent(ImageToImageIntent.UpdateUpscaler(it)) },
                    onConfigure = { processIntent(ImageToImageIntent.ConfigureUpscaler) },
                    modifier = Modifier.padding(top = 16.dp),
                    isEnabled = !state.generating
                )
            }

        }
    }
}

/**
 * Shows source-image acquisition controls or an admitted sampled preview with mask/edit actions.
 *
 * @param state Source selection, acquisition progress and provider capabilities.
 * @param strings Localized source-image action labels.
 * @param image Sampled source preview, or null while absent/unavailable.
 * @param processIntent Requests source selection or clearing through the form owner.
 * @param onInPaintClick Opens the mask editor when the provider supports it.
 * @param modifier Layout modifiers for the input-image surface.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun ImageInputSection(
    state: ImageToImageState,
    strings: ImageToImageStrings,
    image: ImageBitmap?,
    processIntent: (ImageToImageIntent) -> Unit,
    onInPaintClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GenerationImageInput(
        image = image, hasImage = state.imageBase64.isNotBlank(),
        isPickingImage = state.pickingImage, isEnabled = !state.generating,
        onGalleryClick = { processIntent(ImageToImageIntent.PickGallery) },
        onCameraClick = { processIntent(ImageToImageIntent.PickCamera) },
        onRandomClick = { processIntent(ImageToImageIntent.PickRandom) },
        onHistoryImageSelected = { processIntent(ImageToImageIntent.ApplyImageInput(it)) },
        onClearClick = { processIntent(ImageToImageIntent.ClearImageInput) },
        modifier = modifier,
        inPaint = state.inPaint.takeIf { state.sourceSupportsInPaint },
        onInPaintClick = onInPaintClick,
        clearLabel = strings.clear, inPaintLabel = strings.inPaint,
    )
}
