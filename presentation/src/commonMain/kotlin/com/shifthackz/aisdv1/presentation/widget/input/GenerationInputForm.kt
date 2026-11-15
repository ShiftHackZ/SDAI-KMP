package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Displays basic provider inputs in a Column for callers that do not own a lazy list.
 * Generation screens emit the same shared controls as separate lazy items; this wrapper preserves
 * their ordering, editing state and event contract without including advanced controls.
 *
 * @param modifier Compose modifier applied to the rendered UI.
 * @param state Current inputs, provider capabilities and validation feedback.
 * @param isImg2Img Selects image-to-image-specific fields and provider options.
 * @param textFieldContainerColor Background shared by the editable text fields.
 * @param promptChipTextFieldState Prompt editing selection and chip-field text owned by the caller.
 * @param negativePromptChipTextFieldState Negative-prompt editing selection and chip-field text.
 * @param onEvent Sends user edits and actions to the screen's state owner.
 * @author Dmitriy Moroz
 */
@Composable
fun GenerationInputForm(
    modifier: Modifier = Modifier,
    state: GenerationInputFormState,
    isImg2Img: Boolean = false,
    textFieldContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    onEvent: (GenerationInputFormEvent) -> Unit = {},
) {
    Column(modifier = modifier) {
        generationInputBaseFields(state).forEach { field ->
            GenerationInputBaseOption(
                field = field,
                state = state,
                isImg2Img = isImg2Img,
                textFieldContainerColor = textFieldContainerColor,
                promptChipTextFieldState = promptChipTextFieldState,
                negativePromptChipTextFieldState = negativePromptChipTextFieldState,
                onEvent = onEvent,
            )
        }
    }
}
