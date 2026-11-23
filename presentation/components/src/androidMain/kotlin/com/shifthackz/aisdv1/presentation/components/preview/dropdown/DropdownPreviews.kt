package com.shifthackz.aisdv1.presentation.components.preview.dropdown

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.input.DropdownTextField
import com.shifthackz.aisdv1.presentation.components.widget.input.MultiSelectDropdownField
import androidx.compose.ui.Modifier

/**
 * Single and multi-selection fields in loading, empty, selected, overflow and open-menu states.
 *
 * @author Dmitriy Moroz
 */
internal class DropdownPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("DropdownTextField · Empty") {
            DropdownTextField(
                label = "Sampler".asUiText(),
                value = null,
                items = emptyList<String>()
            )
        },
        ComponentPreviewCase("DropdownTextField · Selected") {
            DropdownTextField(
                label = "Sampler".asUiText(),
                value = "Euler",
                items = listOf("Euler", "DPM++")
            )
        },
        ComponentPreviewCase("DropdownTextField · Loading") {
            DropdownTextField(label = "Sampler".asUiText(), value = "Euler", loading = true)
        },
        ComponentPreviewCase("DropdownTextField · Long label") {
            DropdownTextField(
                label = "Sampler".asUiText(),
                value = "A very long selected model name across the visible field"
            )
        },
        ComponentPreviewCase("DropdownTextField · Expanded") {
            DropdownTextField(
                label = "Sampler".asUiText(),
                value = "Euler",
                items = listOf("Euler", "DPM++"),
                isInitiallyExpanded = true
            )
        },
        ComponentPreviewCase("MultiSelectDropdownField · Empty") {
            MultiSelectDropdownField(
                label = "Styles".asUiText(),
                selectedItems = emptyList(),
                availableItems = listOf("Portrait", "Landscape", "Macro", "Illustration")
            )
        },
        ComponentPreviewCase("MultiSelectDropdownField · One") {
            MultiSelectDropdownField(
                label = "Styles".asUiText(),
                selectedItems = listOf("Portrait"),
                availableItems = listOf("Portrait", "Landscape", "Macro", "Illustration")
            )
        },
        ComponentPreviewCase("MultiSelectDropdownField · Two") {
            MultiSelectDropdownField(
                label = "Styles".asUiText(),
                selectedItems = listOf("Portrait", "Landscape"),
                availableItems = listOf("Portrait", "Landscape", "Macro", "Illustration")
            )
        },
        ComponentPreviewCase("MultiSelectDropdownField · Many") {
            MultiSelectDropdownField(
                label = "Styles".asUiText(),
                selectedItems = listOf("Portrait", "Landscape", "Macro", "Illustration"),
                availableItems = listOf("Portrait", "Landscape", "Macro", "Illustration")
            )
        },
        ComponentPreviewCase("MultiSelectDropdownField · Expanded") {
            MultiSelectDropdownField(
                label = "Styles".asUiText(),
                selectedItems = listOf("Portrait"),
                availableItems = listOf("Portrait", "Landscape", "Macro"),
                isInitiallyExpanded = true
            )
        },
    ),
)

/**
 * Renders named dropdown states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 520)
@Preview(name = "Dark", widthDp = 400, heightDp = 520, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 520, fontScale = 1.5f)
@Composable
private fun DropdownPreview(@PreviewParameter(DropdownPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
