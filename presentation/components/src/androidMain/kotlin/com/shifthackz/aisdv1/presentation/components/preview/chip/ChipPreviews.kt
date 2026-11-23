package com.shifthackz.aisdv1.presentation.components.preview.chip

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.model.ExtraType
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.input.chip.ChipTextField
import com.shifthackz.aisdv1.presentation.components.widget.input.chip.ChipTextFieldItem
import com.shifthackz.aisdv1.presentation.components.widget.input.chip.ChipTextFieldWithItem
import androidx.compose.ui.Modifier

/**
 * Prompt chips by extra type, deletion, wrapping and editor draft state, including both generic APIs.
 *
 * @author Dmitriy Moroz
 */
internal class ChipPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("ChipTextFieldItem · null · Delete false") {
            ChipTextFieldItem(text = "cinematic-lighting", type = null, showDeleteIcon = false)
        },
        ComponentPreviewCase("ChipTextFieldItem · null · Delete true") {
            ChipTextFieldItem(text = "cinematic-lighting", type = null, showDeleteIcon = true)
        },
        ComponentPreviewCase("ChipTextFieldItem · ExtraType.Lora · Delete false") {
            ChipTextFieldItem(
                text = "cinematic-lighting",
                type = ExtraType.Lora,
                showDeleteIcon = false
            )
        },
        ComponentPreviewCase("ChipTextFieldItem · ExtraType.Lora · Delete true") {
            ChipTextFieldItem(
                text = "cinematic-lighting",
                type = ExtraType.Lora,
                showDeleteIcon = true
            )
        },
        ComponentPreviewCase("ChipTextFieldItem · ExtraType.HyperNet · Delete false") {
            ChipTextFieldItem(
                text = "cinematic-lighting",
                type = ExtraType.HyperNet,
                showDeleteIcon = false
            )
        },
        ComponentPreviewCase("ChipTextFieldItem · ExtraType.HyperNet · Delete true") {
            ChipTextFieldItem(
                text = "cinematic-lighting",
                type = ExtraType.HyperNet,
                showDeleteIcon = true
            )
        },
        ComponentPreviewCase("ChipTextFieldItem · Long ellipsis") {
            ChipTextFieldItem(
                text = "A very long prompt tag that exceeds the available chip width",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                showDeleteIcon = true
            )
        },
        ComponentPreviewCase("ChipTextFieldWithItem · Empty") {
            ChipTextFieldWithItem(
                textFieldValueState = remember { mutableStateOf(TextFieldValue("")) },
                label = "Prompt".asUiText(),
                list = emptyList(),
                onEvent = {})
        },
        ComponentPreviewCase("ChipTextFieldWithItem · Tags") {
            ChipTextFieldWithItem(
                textFieldValueState = remember { mutableStateOf(TextFieldValue("")) },
                label = "Prompt".asUiText(),
                list = listOf("mountain", "lake", "soft light"),
                onEvent = {})
        },
        ComponentPreviewCase("ChipTextFieldWithItem · Draft") {
            ChipTextFieldWithItem(
                textFieldValueState = remember { mutableStateOf(TextFieldValue("golden hour")) },
                label = "Prompt".asUiText(),
                list = listOf("mountain"),
                onEvent = {})
        },
        ComponentPreviewCase("ChipTextFieldWithItem · Wrapped") {
            ChipTextFieldWithItem(
                textFieldValueState = remember { mutableStateOf(TextFieldValue("")) },
                label = "Prompt".asUiText(),
                list = listOf(
                    "mountain landscape",
                    "cinematic light",
                    "very detailed",
                    "lake reflection",
                    "blue sky"
                ),
                onEvent = {})
        },
        ComponentPreviewCase("ChipTextField · String") {
            ChipTextField(
                chips = listOf("one", "two"),
                chipEventListener = {},
                chipContent = { _, item -> ChipTextFieldItem(text = item) })
        },
        ComponentPreviewCase("ChipTextField · Generic") {
            ChipTextField(
                chips = listOf(1, 2, 3),
                chipTextToItemMapper = { it.toIntOrNull() ?: 0 },
                chipItemToTextMapper = { it.toString() },
                chipEventListener = {},
                chipContent = { _, item -> ChipTextFieldItem(text = "Tag $item") })
        },
    ),
)

/**
 * Renders named chip states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 420)
@Preview(name = "Dark", widthDp = 400, heightDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun ChipPreview(@PreviewParameter(ChipPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
