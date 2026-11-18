package com.shifthackz.aisdv1.presentation.generation.img2img

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArtTrack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.compactDisplayName
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.components.widget.image.rememberPreviewImage
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormEvent
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar

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
) {
    val listState = rememberLazyListState()
    val urlLauncher = rememberExternalUrlLauncher()
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .verticalScrollbar(listState),
        state = listState,
        contentPadding = PaddingValues(16.dp),
    ) {
        item(key = "generation-input-image") {
            BoxWithConstraints(Modifier.padding(bottom = 16.dp)) {
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
            strings = strings,
            promptChipTextFieldState = promptChipTextFieldState,
            negativePromptChipTextFieldState = negativePromptChipTextFieldState,
            onEvent = onEvent,
            afterSlidersSection = afterSlidersSection,
        )

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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.mode.compactDisplayName(state.platform),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.W600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = strings.inputImage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (state.pickingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                    )
                }
            }

            if (image != null) {
                SelectedImageInput(
                    image = image,
                    inPaint = if (state.sourceSupportsInPaint) {
                        state.inPaint
                    } else {
                        ImageInPaintState()
                    },
                    strings = strings,
                    enabled = !state.generating,
                    inPaintVisible = state.sourceSupportsInPaint,
                    onInPaintClick = onInPaintClick,
                    onClearClick = { processIntent(ImageToImageIntent.ClearImageInput) },
                )
            } else {
                EmptyImageInputActions(
                    strings = strings,
                    enabled = !state.pickingImage && !state.generating,
                    onGalleryClick = { processIntent(ImageToImageIntent.PickGallery) },
                    onCameraClick = { processIntent(ImageToImageIntent.PickCamera) },
                    onRandomClick = { processIntent(ImageToImageIntent.PickRandom) },
                )
            }
        }
    }
}

/**
 * Offers gallery, camera and random-gallery acquisition when no source preview is available.
 *
 * @param strings Localized acquisition labels.
 * @param enabled Whether another acquisition/generation operation permits input changes.
 * @param onGalleryClick Requests native photo selection.
 * @param onCameraClick Requests native camera capture.
 * @param onRandomClick Requests a random saved gallery image.
 * @param modifier Layout modifiers for the action group.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun EmptyImageInputActions(
    strings: ImageToImageStrings,
    enabled: Boolean,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRandomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ImagePickerTileButton(
                modifier = Modifier.weight(1f),
                enabled = enabled,
                icon = Icons.Default.Image,
                text = strings.pickGallery,
                onClick = onGalleryClick,
            )
            ImagePickerTileButton(
                modifier = Modifier.weight(1f),
                enabled = enabled,
                icon = Icons.Default.Camera,
                text = strings.pickCamera,
                onClick = onCameraClick,
            )
        }
        ImagePickerButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            icon = Icons.Default.ArtTrack,
            text = strings.pickRandom,
            onClick = onRandomClick,
        )
    }
}

/**
 * Shows an existing source image and its mask with inpaint and clear actions.
 *
 * @param image Admitted sampled source pixels.
 * @param inPaint Stored mask strokes projected onto the preview.
 * @param strings Localized edit/clear labels.
 * @param enabled Whether source editing is currently allowed.
 * @param inPaintVisible Whether this provider exposes mask editing.
 * @param onInPaintClick Opens the full mask editor.
 * @param onClearClick Requests removal of the source image.
 * @param modifier Layout modifiers for the preview/action group.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun SelectedImageInput(
    image: ImageBitmap,
    inPaint: ImageInPaintState,
    strings: ImageToImageStrings,
    enabled: Boolean,
    inPaintVisible: Boolean,
    onInPaintClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        ImageInPaintCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .aspectRatio(1f),
            image = image,
            state = inPaint,
            drawEnabled = false,
            maskAlpha = 1f,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (inPaintVisible) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = enabled,
                    onClick = onInPaintClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = strings.inPaint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = enabled,
                onClick = onClearClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = strings.clear,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
