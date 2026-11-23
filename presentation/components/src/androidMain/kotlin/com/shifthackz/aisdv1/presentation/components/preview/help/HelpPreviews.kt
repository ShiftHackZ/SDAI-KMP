@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.components.preview.help

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import com.shifthackz.aisdv1.presentation.components.widget.help.PlatformRichTooltip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpAnchor
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpCircleIconButton
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpIconButton
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpMenuItem
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpNavigationBarItem
import androidx.compose.ui.Modifier

/**
 * Help anchors, open short/long tooltips, menu actions and navigation selection/disabled states.
 *
 * @author Dmitriy Moroz
 */
internal class HelpPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("ContextualHelp · Anchor") {
            ContextualHelp(
                title = "Sampling steps",
                message = "Higher values take more time."
            ) { Text("Long press for help") }
        },
        ComponentPreviewCase("ContextualHelpAnchor · Open") {
            ContextualHelpAnchor(
                title = "Sampling steps",
                message = "Controls the number of denoising steps.",
                titleStartIcon = Icons.Default.Settings,
                state = rememberTooltipState(initialIsVisible = true, isPersistent = true)
            ) { Text("Sampling steps") }
        },
        ComponentPreviewCase("ContextualHelpAnchor · Long open") {
            ContextualHelpAnchor(
                title = "Generation setting with a long heading",
                message = "A longer explanation of the setting and how it affects generation. ".repeat(
                    5
                ),
                state = rememberTooltipState(initialIsVisible = true, isPersistent = true)
            ) { Text("Help") }
        },
        ComponentPreviewCase("ContextualHelpMenuItem") {
            ContextualHelpMenuItem(
                title = "Gallery",
                message = "Browse generated images.",
                icon = Icons.Default.Settings,
                onClick = {})
        },
        ComponentPreviewCase("ContextualHelpIconButton · true") {
            ContextualHelpIconButton(
                title = "Settings",
                message = "Configure generation.",
                icon = Icons.Default.Settings,
                onClick = {},
                isEnabled = true
            )
        },
        ComponentPreviewCase("ContextualHelpCircleIconButton · true") {
            ContextualHelpCircleIconButton(
                title = "Settings",
                message = "Configure generation.",
                icon = Icons.Default.Settings,
                onClick = {},
                isEnabled = true
            )
        },
        ComponentPreviewCase("ContextualHelpNavigationBarItem · true") {
            Row {
                ContextualHelpNavigationBarItem(
                    title = "Gallery",
                    message = "Browse generated images.",
                    icon = Icons.Default.Settings,
                    isSelected = true,
                    onClick = {})
            }
        },
        ComponentPreviewCase("ContextualHelpIconButton · false") {
            ContextualHelpIconButton(
                title = "Settings",
                message = "Configure generation.",
                icon = Icons.Default.Settings,
                onClick = {},
                isEnabled = false
            )
        },
        ComponentPreviewCase("ContextualHelpCircleIconButton · false") {
            ContextualHelpCircleIconButton(
                title = "Settings",
                message = "Configure generation.",
                icon = Icons.Default.Settings,
                onClick = {},
                isEnabled = false
            )
        },
        ComponentPreviewCase("ContextualHelpNavigationBarItem · false") {
            Row {
                ContextualHelpNavigationBarItem(
                    title = "Gallery",
                    message = "Browse generated images.",
                    icon = Icons.Default.Settings,
                    isSelected = false,
                    onClick = {})
            }
        },
        ComponentPreviewCase("PlatformRichTooltip") {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                state = rememberTooltipState(initialIsVisible = true, isPersistent = true),
                tooltip = { PlatformRichTooltip(title = { Text("Help") }) { Text("A clear explanation of the selected option.") } }) {
                Text(
                    "Help anchor"
                )
            }
        },
    ),
)

/**
 * Renders named help states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 620)
@Preview(name = "Dark", widthDp = 400, heightDp = 620, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 620, fontScale = 1.5f)
@Composable
private fun HelpPreview(@PreviewParameter(HelpPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
