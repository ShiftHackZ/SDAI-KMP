package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.presentation.components.widget.input.DropdownTextField

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.OpenAiQuality
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionComponent


/**
 * Renders one basic generation control for both the lazy screens and the public Column wrapper.
 * Each control keeps its original values and event owner; this dispatcher adds no business state.
 *
 * @param field Stable logical basic control selected before composition.
 * @param state Current provider inputs and validation feedback.
 * @param isImg2Img Selects image-to-image provider capabilities.
 * @param textFieldContainerColor Background of plain prompt editors.
 * @param promptChipTextFieldState Caller-owned positive prompt chip text and selection.
 * @param negativePromptChipTextFieldState Caller-owned negative prompt chip text and selection.
 * @param onEvent Receives generation edits and actions.
 * @param engineContent Checkpoint picker supplied by the owning form.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationInputBaseOption(
    field: GenerationBaseFormField,
    state: GenerationInputFormState,
    isImg2Img: Boolean,
    textFieldContainerColor: Color,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    onEvent: (GenerationInputFormEvent) -> Unit,
    engineContent: @Composable (Modifier) -> Unit = { EngineSelectionComponent(modifier = it) },
) {
    when (field) {
        GenerationBaseFormField.PROVIDER,
        GenerationBaseFormField.MNN_BACKEND,
        GenerationBaseFormField.SDXL_BACKEND,
        GenerationBaseFormField.BONSAI_BACKEND -> GenerationBaseProviderOption(
            field,
            state,
            isImg2Img,
            onEvent,
            engineContent,
        )

        GenerationBaseFormField.COMPATIBLE_MODEL -> OpenAiCompatibleModelInput(state, onEvent)
        GenerationBaseFormField.COMPATIBLE_DIMENSIONS -> OpenAiCompatibleDimensionsInput(
            state,
            onEvent
        )

        GenerationBaseFormField.COMPATIBLE_BATCH,
        GenerationBaseFormField.OPENAI_BATCH -> GenerationBatchComponent(
            state.mode,
            state.batchCount,
            onEvent
        )

        GenerationBaseFormField.PROMPT,
        GenerationBaseFormField.NEGATIVE_PROMPT -> {
            val isNegative = field == GenerationBaseFormField.NEGATIVE_PROMPT
            GenerationPromptEditor(
                prompt = state.prompt,
                negativePrompt = state.negativePrompt,
                keywords = if (isNegative) state.negativePromptKeywords else state.promptKeywords,
                isNegative = isNegative,
                isTaggedInputEnabled = state.formPromptTaggedInput,
                textFieldContainerColor = textFieldContainerColor,
                chipTextFieldState = if (isNegative) negativePromptChipTextFieldState else promptChipTextFieldState,
                onEvent = onEvent,
            )
        }

        GenerationBaseFormField.DIMENSIONS -> GenerationBaseDimensions(state, isImg2Img, onEvent)
        GenerationBaseFormField.OPENAI_QUALITY -> {

            DropdownTextField(
                modifier = Modifier.padding(top = 8.dp),
                label = Localization.string("hint_quality").asUiText(),
                value = state.openAiQuality,
                items = OpenAiQuality.entries,
                onItemSelected = { onEvent(GenerationInputFormEvent.UpdateOpenAiQuality(it)) },
                displayDelegate = { it.key.asUiText() },
            )

        }

        GenerationBaseFormField.ADVANCED_TOGGLE -> {
            val areAdvancedOptionsVisible = state.advancedOptionsVisible
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

                TextButton(
                    modifier = Modifier,
                    onClick = {
                        onEvent(
                            GenerationInputFormEvent.UpdateAdvancedOptionsVisibility(
                                !areAdvancedOptionsVisible,
                            )
                        )
                    },
                ) {
                    Icon(
                        imageVector = if (areAdvancedOptionsVisible) Icons.Default.ArrowDropUp
                        else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                    )
                    Text(
                        text = Localization.string(
                            if (areAdvancedOptionsVisible) "action_options_hide"
                            else "action_options_show",
                        )
                    )
                }

            }
        }
    }
}
