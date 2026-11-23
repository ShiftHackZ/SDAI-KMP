package com.shifthackz.aisdv1.presentation.components.preview.action

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.action.CircleIconButton
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryActionButton
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import com.shifthackz.aisdv1.presentation.components.widget.action.ScreenActionBar
import com.shifthackz.aisdv1.presentation.components.widget.action.SecondaryActionButton
import androidx.compose.ui.Modifier

/**
 * Enabled, disabled and long-label buttons plus single and stacked screen actions.
 *
 * @author Dmitriy Moroz
 */
internal class ActionPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("PrimaryButton · Enabled") {
            PrimaryButton(onClick = {}, isEnabled = true) { Text("Continue") }
        },
        ComponentPreviewCase("PrimaryButton · Disabled") {
            PrimaryButton(onClick = {}, isEnabled = false) { Text("Continue") }
        },
        ComponentPreviewCase("PrimaryButton · Long label") {
            PrimaryButton(
                onClick = {},
                isEnabled = true
            ) { Text("Continue with the selected generation configuration") }
        },
        ComponentPreviewCase("OutlinedButton · Enabled") {
            OutlinedButton(onClick = {}, isEnabled = true) { Text("Continue") }
        },
        ComponentPreviewCase("OutlinedButton · Disabled") {
            OutlinedButton(onClick = {}, isEnabled = false) { Text("Continue") }
        },
        ComponentPreviewCase("OutlinedButton · Long label") {
            OutlinedButton(
                onClick = {},
                isEnabled = true
            ) { Text("Continue with the selected generation configuration") }
        },
        ComponentPreviewCase("PrimaryActionButton · Enabled") {
            PrimaryActionButton(onClick = {}, isEnabled = true) { Text("Continue") }
        },
        ComponentPreviewCase("PrimaryActionButton · Disabled") {
            PrimaryActionButton(onClick = {}, isEnabled = false) { Text("Continue") }
        },
        ComponentPreviewCase("PrimaryActionButton · Long label") {
            PrimaryActionButton(
                onClick = {},
                isEnabled = true
            ) { Text("Continue with the selected generation configuration") }
        },
        ComponentPreviewCase("SecondaryActionButton · Enabled") {
            SecondaryActionButton(onClick = {}, isEnabled = true) { Text("Continue") }
        },
        ComponentPreviewCase("SecondaryActionButton · Disabled") {
            SecondaryActionButton(onClick = {}, isEnabled = false) { Text("Continue") }
        },
        ComponentPreviewCase("SecondaryActionButton · Long label") {
            SecondaryActionButton(
                onClick = {},
                isEnabled = true
            ) { Text("Continue with the selected generation configuration") }
        },
        ComponentPreviewCase("CircleIconButton · true") {
            CircleIconButton(
                onClick = {},
                icon = Icons.Default.Add,
                contentDescription = "Add",
                isEnabled = true
            )
        },
        ComponentPreviewCase("CircleIconButton · false") {
            CircleIconButton(
                onClick = {},
                icon = Icons.Default.Add,
                contentDescription = "Add",
                isEnabled = false
            )
        },
        ComponentPreviewCase("ScreenActionBar · Single") {
            ScreenActionBar { PrimaryActionButton(onClick = {}) { Text("Continue") } }
        },
        ComponentPreviewCase("ScreenActionBar · Stacked") {
            ScreenActionBar {
                PrimaryActionButton(onClick = {}) { Text("Continue") }; SecondaryActionButton(
                onClick = {}) { Text("Back") }
            }
        },
    ),
)

/**
 * Renders named action states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 420)
@Preview(name = "Dark", widthDp = 400, heightDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun ActionPreview(@PreviewParameter(ActionPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
