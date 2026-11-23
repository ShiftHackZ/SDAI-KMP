package com.shifthackz.aisdv1.presentation.components.preview.input

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformOutlinedTextField
import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformSlider
import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformSwitch
import com.shifthackz.aisdv1.presentation.components.widget.input.SearchField
import com.shifthackz.aisdv1.presentation.components.widget.input.SliderTextInputContent
import com.shifthackz.aisdv1.presentation.components.widget.input.SliderTextInputField
import androidx.compose.ui.Modifier

/**
 * Text fields, searches, switch combinations and numeric sliders including typed draft validation.
 *
 * @author Dmitriy Moroz
 */
internal class InputPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("PlatformOutlinedTextField · Empty") {
            PlatformOutlinedTextField(
                value = "",
                onValueChange = {},
                label = "Value",
                containerColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface,
                hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                enabled = true,
                error = null
            )
        },
        ComponentPreviewCase("PlatformOutlinedTextField · Filled") {
            PlatformOutlinedTextField(
                value = "A misty mountain lake",
                onValueChange = {},
                label = "Value",
                containerColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface,
                hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                enabled = true,
                error = null
            )
        },
        ComponentPreviewCase("PlatformOutlinedTextField · Disabled") {
            PlatformOutlinedTextField(
                value = "Unavailable",
                onValueChange = {},
                label = "Value",
                containerColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface,
                hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                enabled = false,
                error = null
            )
        },
        ComponentPreviewCase("PlatformOutlinedTextField · Error") {
            PlatformOutlinedTextField(
                value = "bad endpoint",
                onValueChange = {},
                label = "Value",
                containerColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface,
                hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                enabled = true,
                error = "Enter a valid address"
            )
        },
        ComponentPreviewCase("PlatformOutlinedTextField · Multiline") {
            PlatformOutlinedTextField(
                value = "First line\nSecond line\nThird line",
                onValueChange = {},
                label = "Value",
                containerColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface,
                hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                enabled = true,
                error = null
            )
        },
        ComponentPreviewCase("PlatformOutlinedTextField · Long value") {
            PlatformOutlinedTextField(
                value = "A long value that must remain legible in a narrow text field",
                onValueChange = {},
                label = "Value",
                containerColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface,
                hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                enabled = true,
                error = null
            )
        },
        ComponentPreviewCase("Password") {
            PlatformOutlinedTextField(
                value = "secret",
                onValueChange = {},
                label = "Password",
                containerColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface,
                hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                visualTransformation = PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password
            )
        },
        ComponentPreviewCase("SearchField · Empty") {
            SearchField(value = "", onValueChange = {}, placeholder = "Search")
        },
        ComponentPreviewCase("SearchField · mountain") {
            SearchField(value = "mountain", onValueChange = {}, placeholder = "Search")
        },
        ComponentPreviewCase("SearchField · A long search term that exceeds the visible input width") {
            SearchField(
                value = "A long search term that exceeds the visible input width",
                onValueChange = {},
                placeholder = "Search"
            )
        },
        ComponentPreviewCase("PlatformSwitch · true · true") {
            PlatformSwitch(isChecked = true, onCheckedChange = {}, isEnabled = true)
        },
        ComponentPreviewCase("PlatformSwitch · true · false") {
            PlatformSwitch(isChecked = true, onCheckedChange = {}, isEnabled = false)
        },
        ComponentPreviewCase("PlatformSwitch · false · true") {
            PlatformSwitch(isChecked = false, onCheckedChange = {}, isEnabled = true)
        },
        ComponentPreviewCase("PlatformSwitch · false · false") {
            PlatformSwitch(isChecked = false, onCheckedChange = {}, isEnabled = false)
        },
        ComponentPreviewCase("PlatformSlider · 0f") {
            PlatformSlider(value = 0f, onValueChange = {}, contentDescription = "Strength")
        },
        ComponentPreviewCase("PlatformSlider · 0.5f") {
            PlatformSlider(value = 0.5f, onValueChange = {}, contentDescription = "Strength")
        },
        ComponentPreviewCase("PlatformSlider · 1f") {
            PlatformSlider(value = 1f, onValueChange = {}, contentDescription = "Strength")
        },
        ComponentPreviewCase("PlatformSlider · Disabled") {
            PlatformSlider(value = 0.5f, onValueChange = {}, isEnabled = false)
        },
        ComponentPreviewCase("PlatformSlider · Discrete") {
            PlatformSlider(value = 0.5f, onValueChange = {}, steps = 3)
        },
        ComponentPreviewCase("SliderTextInputField · Float") {
            SliderTextInputField(value = 0.5f, onValueChange = {})
        },
        ComponentPreviewCase("SliderTextInputField · Double") {
            SliderTextInputField(value = 0.5, onValueChange = {})
        },
        ComponentPreviewCase("SliderTextInputField · Minimum") {
            SliderTextInputField(value = 0f, onValueChange = {}, fractionDigits = 0)
        },
        ComponentPreviewCase("SliderTextInputField · Maximum") {
            SliderTextInputField(value = 1f, onValueChange = {})
        },
        ComponentPreviewCase("Numeric draft · Apply valid draft") {
            SliderTextInputContent(
                value = 0.5f,
                text = "0.75",
                isNotApplied = true,
                isValid = true,
                onTextChange = {},
                onFocusChanged = {},
                onValueChange = {})
        },
        ComponentPreviewCase("Numeric draft · Invalid text") {
            SliderTextInputContent(
                value = 0.5f,
                text = "abc",
                isNotApplied = true,
                isValid = false,
                onTextChange = {},
                onFocusChanged = {},
                onValueChange = {})
        },
        ComponentPreviewCase("Numeric draft · Out of range") {
            SliderTextInputContent(
                value = 0.5f,
                text = "2.0",
                isNotApplied = true,
                isValid = false,
                onTextChange = {},
                onFocusChanged = {},
                onValueChange = {})
        },
        ComponentPreviewCase("Numeric draft · Empty draft") {
            SliderTextInputContent(
                value = 0.5f,
                text = "",
                isNotApplied = true,
                isValid = false,
                onTextChange = {},
                onFocusChanged = {},
                onValueChange = {})
        },
    ),
)

/**
 * Renders named input states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 420)
@Preview(name = "Dark", widthDp = 400, heightDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun InputPreview(@PreviewParameter(InputPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
