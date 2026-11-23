package com.shifthackz.aisdv1.presentation.components.preview.toolbar

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryActionButton
import com.shifthackz.aisdv1.presentation.components.widget.action.ScreenActionBar
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformTabBar
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.section.ExpandableSectionHeader
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.GenerationBottomToolbar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.GenerationHistoryButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ModalDialogToolbar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarNavigationButton

/**
 * Expandable sections, toolbar actions, loading chrome and provider-specific generation shortcuts.
 *
 * @author Dmitriy Moroz
 */
internal class ToolbarPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("ExpandableSectionHeader · false") {
            ExpandableSectionHeader(
                title = "Advanced options",
                isExpanded = false,
                onToggleExpanded = {})
        },
        ComponentPreviewCase("ExpandableSectionHeader · true") {
            ExpandableSectionHeader(
                title = "Advanced options",
                isExpanded = true,
                onToggleExpanded = {})
        },
        ComponentPreviewCase("ExpandableSectionHeader · Trailing") {
            ExpandableSectionHeader(
                title = "Input image",
                isExpanded = true,
                onToggleExpanded = {},
                trailingContent = { CircularProgressIndicator(Modifier.size(24.dp)) })
        },
        ComponentPreviewCase("ToolbarIconButton · true") {
            ToolbarIconButton(onClick = {}, isEnabled = true) {
                Icon(
                    Icons.Default.Settings,
                    "Settings"
                )
            }
        },
        ComponentPreviewCase("ToolbarBackButton · true") {
            ToolbarBackButton(onClick = {}, isEnabled = true)
        },
        ComponentPreviewCase("ToolbarNavigationButton · Back true") {
            ToolbarNavigationButton(canNavigateBack = true, onClick = {})
        },
        ComponentPreviewCase("ModalDialogToolbar · Loading true") {
            ModalDialogToolbar(text = "Select model", onClose = {}, loading = true)
        },
        ComponentPreviewCase("ToolbarIconButton · false") {
            ToolbarIconButton(onClick = {}, isEnabled = false) {
                Icon(
                    Icons.Default.Settings,
                    "Settings"
                )
            }
        },
        ComponentPreviewCase("ToolbarBackButton · false") {
            ToolbarBackButton(onClick = {}, isEnabled = false)
        },
        ComponentPreviewCase("ToolbarNavigationButton · Back false") {
            ToolbarNavigationButton(canNavigateBack = false, onClick = {})
        },
        ComponentPreviewCase("ModalDialogToolbar · Loading false") {
            ModalDialogToolbar(text = "Select model", onClose = {}, loading = false)
        },
        ComponentPreviewCase("GenerationHistoryButton") {
            GenerationHistoryButton(onClick = {})
        },
        ComponentPreviewCase("GenerationBottomToolbar · AUTOMATIC1111 · Accent false") {
            GenerationBottomToolbar(
                mode = ServerSource.AUTOMATIC1111,
                prompt = "Mountain",
                negativePrompt = "Blur",
                strokeAccentState = false
            ) { PrimaryActionButton(onClick = {}) { Text("Generate") } }
        },
        ComponentPreviewCase("GenerationBottomToolbar · AUTOMATIC1111 · Accent true") {
            GenerationBottomToolbar(
                mode = ServerSource.AUTOMATIC1111,
                prompt = "Mountain",
                negativePrompt = "Blur",
                strokeAccentState = true
            ) { PrimaryActionButton(onClick = {}) { Text("Generate") } }
        },
        ComponentPreviewCase("GenerationBottomToolbar · SWARM_UI · Accent false") {
            GenerationBottomToolbar(
                mode = ServerSource.SWARM_UI,
                prompt = "Mountain",
                negativePrompt = "Blur",
                strokeAccentState = false
            ) { PrimaryActionButton(onClick = {}) { Text("Generate") } }
        },
        ComponentPreviewCase("GenerationBottomToolbar · SWARM_UI · Accent true") {
            GenerationBottomToolbar(
                mode = ServerSource.SWARM_UI,
                prompt = "Mountain",
                negativePrompt = "Blur",
                strokeAccentState = true
            ) { PrimaryActionButton(onClick = {}) { Text("Generate") } }
        },
        ComponentPreviewCase("GenerationBottomToolbar · OPEN_AI · Accent false") {
            GenerationBottomToolbar(
                mode = ServerSource.OPEN_AI,
                prompt = "Mountain",
                negativePrompt = "Blur",
                strokeAccentState = false
            ) { PrimaryActionButton(onClick = {}) { Text("Generate") } }
        },
        ComponentPreviewCase("GenerationBottomToolbar · OPEN_AI · Accent true") {
            GenerationBottomToolbar(
                mode = ServerSource.OPEN_AI,
                prompt = "Mountain",
                negativePrompt = "Blur",
                strokeAccentState = true
            ) { PrimaryActionButton(onClick = {}) { Text("Generate") } }
        },
        ComponentPreviewCase("PlatformTopAppBar") {
            PlatformTopAppBar(
                title = { Text("Gallery") },
                navigationIcon = { ToolbarBackButton({}) },
                actions = { ToolbarIconButton({}) { Icon(Icons.Default.Settings, "Settings") } })
        },
        ComponentPreviewCase("PlatformTopAppBar · Bottom content") {
            PlatformTopAppBar(
                title = { Text("Gallery with a long screen title") },
                bottomContentHeight = 48.dp,
                bottomContent = {
                    PlatformTabBar(
                        labels = listOf("Images", "Info"),
                        selectedIndex = 0,
                        onSelect = {})
                })
        },
        ComponentPreviewCase("PlatformScaffold") {
            PlatformScaffold(
                modifier = Modifier.height(360.dp),
                topBar = { PlatformTopAppBar(title = { Text("Screen") }) },
                bottomBar = { ScreenActionBar { PrimaryActionButton({}) { Text("Continue") } } }) { padding ->
                Text(
                    "Screen content",
                    Modifier.padding(padding)
                )
            }
        },
    ),
)

/**
 * Renders named toolbar states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 460)
@Preview(name = "Dark", widthDp = 400, heightDp = 460, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 460, fontScale = 1.5f)
@Composable
private fun ToolbarPreview(@PreviewParameter(ToolbarPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
