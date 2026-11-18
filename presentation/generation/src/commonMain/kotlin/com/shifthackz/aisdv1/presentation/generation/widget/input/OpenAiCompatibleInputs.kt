package com.shifthackz.aisdv1.presentation.generation.widget.input

import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformOutlinedTextField

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Displays the compatible provider's editable model or the fixed OAuth model label.
 *
 * @param state Current provider and arbitrary model name.
 * @param onEvent Receives compatible-model text changes.
 * @param modifier Layout modifiers for the editable model field or fixed OAuth label.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun OpenAiCompatibleModelInput(
    state: GenerationInputFormState,
    onEvent: (GenerationInputFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {

    if (state.mode == ServerSource.OPEN_AI_OAUTH) Text(
        "gpt-image-2",
        modifier = modifier,
    )
    else PlatformOutlinedTextField(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        value = state.openAiCompatibleModel,
        onValueChange = { onEvent(GenerationInputFormEvent.UpdateOpenAiCompatibleModel(it)) },
        label = Localization.string("open_ai_compatible_model"),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        textColor = MaterialTheme.colorScheme.onSurface,
        hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
        singleLine = true,
    )

}

/**
 * Displays unrestricted compatible-provider pixel dimensions as a paired horizontal control.
 *
 * @param state Current dimension text and validation feedback.
 * @param onEvent Receives width and height text edits.
 * @param modifier Layout modifiers for the paired dimension row.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun OpenAiCompatibleDimensionsInput(
    state: GenerationInputFormState,
    onEvent: (GenerationInputFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f).padding(end = 4.dp),
            value = state.width,
            onValueChange = { onEvent(GenerationInputFormEvent.UpdateWidth(it)) },
            label = { Text(Localization.string("width")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.widthValidationError != null,
            supportingText = { state.widthValidationError?.let { Text(it.asString()) } },
            singleLine = true,
        )
        OutlinedTextField(
            modifier = Modifier.weight(1f).padding(start = 4.dp),
            value = state.height,
            onValueChange = { onEvent(GenerationInputFormEvent.UpdateHeight(it)) },
            label = { Text(Localization.string("height")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.heightValidationError != null,
            supportingText = { state.heightValidationError?.let { Text(it.asString()) } },
            singleLine = true,
        )
    }
}
