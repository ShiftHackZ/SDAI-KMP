@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.generation.img2img

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationInputFormEvent
import com.shifthackz.aisdv1.presentation.generation.widget.input.generationInputFormItems

/**
 * Adds shared generation fields and image-to-image validation/progress feedback to a lazy form.
 *
 * @param isExpanded Whether generation fields and non-independent advanced controls are visible.
 * @param onToggleExpanded Toggles generation options without changing advanced preferences.
 * @param state Captured form state and provider-specific validation.
 * @param strings Localized unsupported-provider and error explanations.
 * @param promptChipTextFieldState Pending positive-prompt chip text retained by the screen.
 * @param negativePromptChipTextFieldState Pending negative-prompt chip text retained by the screen.
 * @param onEvent Publishes shared generation-field edits.
 * @param afterSlidersSection Image-to-image controls appended after shared sampling sliders.
 *
 * @author Dmitriy Moroz
 */
internal fun LazyListScope.imageToImageFormItems(
    state: ImageToImageState,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    strings: ImageToImageStrings,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    onEvent: (GenerationInputFormEvent) -> Unit,
    afterSlidersSection: @Composable () -> Unit,
) {
    val hasFooter = state.promptValidationError != null || !state.sourceSupportsImageToImage ||
            state.error != null || state.message != null || state.generating
    generationInputFormItems(
        state = state,
        isImg2Img = true,
        isExpanded = isExpanded,
        onToggleExpanded = onToggleExpanded,
        promptChipTextFieldState = promptChipTextFieldState,
        negativePromptChipTextFieldState = negativePromptChipTextFieldState,
        onEvent = onEvent,
        afterSlidersSection = afterSlidersSection,
        hasFooter = hasFooter,
        footer = {
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
 * Edits the source-image denoising strength within its zero-to-one range.
 *
 * @param denoisingStrength Current source-to-output transformation strength.
 * @param processIntent Publishes denoising changes to the owning ViewModel.
 * @param modifier Layout modifiers for the denoising label and slider.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun DenoisingStrengthSlider(
    denoisingStrength: Float,
    processIntent: (ImageToImageIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
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
