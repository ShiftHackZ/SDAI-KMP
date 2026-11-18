package com.shifthackz.aisdv1.presentation.components.widget.input.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.presentation.components.utils.ExtrasFormatter

/**
 * Combines prompt-tag editing with typed chip colors, deletion and an external per-tag editor.
 *
 * @param modifier Layout modifiers for the prompt-tag field.
 * @param textFieldValueState Caller-owned pending text and selection.
 * @param label Localized field caption.
 * @param list Ordered prompt tags currently shown as chips.
 * @param onItemClick Opens the tag editor with the clicked zero-based index and tag text.
 * @param onEvent Receives requested additions, batch additions, edits or removals.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ChipTextFieldWithItem(
    modifier: Modifier = Modifier,
    textFieldValueState: MutableState<TextFieldValue>,
    label: UiText,
    list: List<String>,
    onItemClick: (type: Int, item: String) -> Unit = { _, _ -> },
    onEvent: (event: ChipTextFieldEvent<String>) -> Unit,
) {
    ChipTextField(
        modifier = modifier,
        label = { Text(label.asString()) },
        textFieldValueState = textFieldValueState,
        textStyle = LocalTextStyle.current,
        chips = list,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        chipEventListener = { onEvent(it) },
    ) { index, item ->
        ChipTextFieldItem(
            text = item,
            type = ExtrasFormatter.determineExtraType(item),
            showDeleteIcon = true,
            onDeleteClick = { onEvent(ChipTextFieldEvent.Remove(index)) },
            onItemClick = {
                onItemClick(index, item)
            }
        )
    }
}
