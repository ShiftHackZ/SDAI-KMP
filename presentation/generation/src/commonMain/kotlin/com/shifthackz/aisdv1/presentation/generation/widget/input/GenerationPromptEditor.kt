package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformOutlinedTextField

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.widget.input.chip.ChipTextFieldWithItem


/**
 * Displays either prompt through one shared editor structure. Text and chip selection remain
 * owned by the screen; scrolling a field out of view does not own or clear its generation value.
 *
 * @param prompt Current positive prompt used when opening a tag editor.
 * @param negativePrompt Current negative prompt used when opening a tag editor.
 * @param keywords Ordered tags belonging to this prompt.
 * @param isNegative Whether edits target the negative prompt.
 * @param isTaggedInputEnabled Whether to display chips instead of a plain multiline editor.
 * @param textFieldContainerColor Existing provider form background for plain text input.
 * @param chipTextFieldState Caller-owned chip editing text and selection.
 * @param onEvent Receives prompt changes and tag-edit requests.
 * @param modifier Layout modifiers applied to the active plain/chip prompt editor.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationPromptEditor(
    prompt: String,
    negativePrompt: String,
    keywords: List<String>,
    isNegative: Boolean,
    isTaggedInputEnabled: Boolean,
    textFieldContainerColor: Color,
    chipTextFieldState: MutableState<TextFieldValue>,
    onEvent: (GenerationInputFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelKey = if (isNegative) "hint_prompt_negative" else "hint_prompt"
    val onPromptChange: (String) -> Unit = { value ->
        onEvent(
            if (isNegative) GenerationInputFormEvent.UpdateNegativePrompt(value)
            else GenerationInputFormEvent.UpdatePrompt(value),
        )
    }
    if (isTaggedInputEnabled) {
        ChipTextFieldWithItem(
            modifier = modifier.fillMaxWidth().padding(top = 8.dp),
            textFieldValueState = chipTextFieldState,
            label = Localization.string(labelKey).asUiText(),
            list = keywords,
            onItemClick = { _, tag ->
                onEvent(GenerationInputFormEvent.EditTag(prompt, negativePrompt, tag, isNegative))
            },
        ) { event -> onPromptChange(processTaggedPrompt(keywords, event)) }
    } else {
        PlatformOutlinedTextField(
            modifier = modifier.fillMaxWidth().padding(top = 8.dp),
            value = if (isNegative) negativePrompt else prompt,
            onValueChange = onPromptChange,
            label = Localization.string(labelKey),
            containerColor = textFieldContainerColor,
            textColor = MaterialTheme.colorScheme.onSurface,
            hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
