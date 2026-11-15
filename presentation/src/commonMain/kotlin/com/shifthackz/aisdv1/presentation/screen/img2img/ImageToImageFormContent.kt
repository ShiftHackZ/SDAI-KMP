@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.img2img

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.presentation.widget.image.SampledBase64Image
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormEvent
import com.shifthackz.aisdv1.presentation.widget.input.generationInputFormItems


/**
 * Emits image-to-image fields into the screen's existing lazy list, preserving the source-image
 * item above this card and mounting only advanced controls needed by the viewport.
 *
 * @receiver The image-to-image screen's vertical lazy list.
 * @param state Current provider values, validation and generation status.
 * @param strings Localized labels and unsupported-provider feedback.
 * @param promptChipTextFieldState Caller-owned prompt chip editing state.
 * @param negativePromptChipTextFieldState Caller-owned negative-prompt chip editing state.
 * @param onEvent Receives edits and extension actions from shared generation fields.
 * @param afterSlidersSection Stable denoising controls supplied by the composing screen.
 * @author Dmitriy Moroz
 */
internal fun LazyListScope.imageToImageFormItems(
    state: ImageToImageState,
    strings: ImageToImageStrings,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    onEvent: (GenerationInputFormEvent) -> Unit,
    afterSlidersSection: @Composable () -> Unit,
) {
    generationInputFormItems(
        state = state,
        isImg2Img = true,
        promptChipTextFieldState = promptChipTextFieldState,
        negativePromptChipTextFieldState = negativePromptChipTextFieldState,
        onEvent = onEvent,
        afterSlidersSection = afterSlidersSection,
        footer = {
            val hasFooter =
                state.promptValidationError != null || !state.sourceSupportsImageToImage ||
                        state.error != null || state.message != null || state.generating
            if (hasFooter) Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.promptValidationError?.let { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (!state.sourceSupportsImageToImage) {
                    Text(
                        strings.sourceUnavailable,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                state.error?.let { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                state.message?.let { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (state.generating) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        },
    )
}

/**
 * Displays image-to-image denoising strength independently of prompt and result state.
 *
 * @param denoisingStrength Current strength in the supported zero-to-one range.
 * @param processIntent Receives slider edits for the owning screen.
 * @author Dmitriy Moroz
 */
@Composable
internal fun DenoisingStrengthSlider(
    denoisingStrength: Float,
    processIntent: (ImageToImageIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = Localization.string(
                    "hint_denoising_strength",
                    denoisingStrength.roundToString(),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = denoisingStrength,
            onValueChange = {
                processIntent(ImageToImageIntent.UpdateDenoisingStrength(it))
            },
            valueRange = 0f..1f,
        )
    }
}

/**
 * Renders the `GeneratedImageItem` UI for the SDAI presentation layer.
 *
 * @param result result value consumed by the API.
 * @param strings strings value consumed by the API.
 * @param savingImage saving image value consumed by the API.
 * @param sharingImage sharing image value consumed by the API.
 * @param processIntent process intent value consumed by the API.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GeneratedImageItem(
    result: AiGenerationResult,
    strings: ImageToImageStrings,
    savingImage: Boolean,
    sharingImage: Boolean,
    processIntent: (ImageToImageIntent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (result.image.isNotBlank()) {
                SampledBase64Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(result.aspectRatio),
                    base64 = result.image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            } else {
                EmptyGeneratedImage(strings.imageUnavailable)
            }

            Text(
                text = result.prompt,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = strings.resultMeta(result),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val actionsEnabled = result.image.isNotBlank() && !savingImage && !sharingImage
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = actionsEnabled,
                    onClick = { processIntent(ImageToImageIntent.SaveResult(result.image)) },
                ) {
                    if (savingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = if (savingImage) strings.savingImage else strings.save,
                    )
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = actionsEnabled,
                    onClick = { processIntent(ImageToImageIntent.ShareResult(result.image)) },
                ) {
                    if (sharingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = if (sharingImage) strings.sharingImage else strings.share,
                    )
                }
            }
        }
    }
}

/**
 * Renders the `EmptyGeneratedImage` UI for the SDAI presentation layer.
 *
 * @param text text value consumed by the API.
 * @author Dmitriy Moroz
 */
@Composable
internal fun EmptyGeneratedImage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
