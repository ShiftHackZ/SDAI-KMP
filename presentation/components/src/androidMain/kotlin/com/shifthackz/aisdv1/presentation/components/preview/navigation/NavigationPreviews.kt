package com.shifthackz.aisdv1.presentation.components.preview.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.navigation.MaterialNavigationBar
import com.shifthackz.aisdv1.presentation.components.widget.navigation.MaterialTabBar
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformDrawerItem
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformDrawerSheet
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformNavigationBar
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformNavigationBarItem
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformTabBar
import com.shifthackz.aisdv1.presentation.components.widget.navigation.ProgrammaticPager
import androidx.compose.ui.Modifier

/**
 * Every selected destination and page, disabled tabs, obscured chrome and drawer selection.
 *
 * @author Dmitriy Moroz
 */
internal class NavigationPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("PlatformNavigationBar · 0") {
            PlatformNavigationBar(items = destinations(0))
        },
        ComponentPreviewCase("MaterialNavigationBar · 0") {
            MaterialNavigationBar(items = destinations(0))
        },
        ComponentPreviewCase("PlatformTabBar · 0") {
            PlatformTabBar(
                labels = listOf("Image", "Input", "Info"),
                selectedIndex = 0,
                onSelect = {})
        },
        ComponentPreviewCase("MaterialTabBar · 0") {
            MaterialTabBar(
                labels = listOf("Image", "Input", "Info"),
                selectedIndex = 0,
                onSelect = {})
        },
        ComponentPreviewCase("ProgrammaticPager · 0") {
            ProgrammaticPager(
                selectedPage = 0,
                pageCount = 3,
                modifier = Modifier.height(240.dp),
                shouldAnimate = false
            ) { Text("Page ${it + 1}") }
        },
        ComponentPreviewCase("PlatformNavigationBar · 1") {
            PlatformNavigationBar(items = destinations(1))
        },
        ComponentPreviewCase("MaterialNavigationBar · 1") {
            MaterialNavigationBar(items = destinations(1))
        },
        ComponentPreviewCase("PlatformTabBar · 1") {
            PlatformTabBar(
                labels = listOf("Image", "Input", "Info"),
                selectedIndex = 1,
                onSelect = {})
        },
        ComponentPreviewCase("MaterialTabBar · 1") {
            MaterialTabBar(
                labels = listOf("Image", "Input", "Info"),
                selectedIndex = 1,
                onSelect = {})
        },
        ComponentPreviewCase("ProgrammaticPager · 1") {
            ProgrammaticPager(
                selectedPage = 1,
                pageCount = 3,
                modifier = Modifier.height(240.dp),
                shouldAnimate = false
            ) { Text("Page ${it + 1}") }
        },
        ComponentPreviewCase("PlatformNavigationBar · 2") {
            PlatformNavigationBar(items = destinations(2))
        },
        ComponentPreviewCase("MaterialNavigationBar · 2") {
            MaterialNavigationBar(items = destinations(2))
        },
        ComponentPreviewCase("PlatformTabBar · 2") {
            PlatformTabBar(
                labels = listOf("Image", "Input", "Info"),
                selectedIndex = 2,
                onSelect = {})
        },
        ComponentPreviewCase("MaterialTabBar · 2") {
            MaterialTabBar(
                labels = listOf("Image", "Input", "Info"),
                selectedIndex = 2,
                onSelect = {})
        },
        ComponentPreviewCase("ProgrammaticPager · 2") {
            ProgrammaticPager(
                selectedPage = 2,
                pageCount = 3,
                modifier = Modifier.height(240.dp),
                shouldAnimate = false
            ) { Text("Page ${it + 1}") }
        },
        ComponentPreviewCase("PlatformNavigationBar · No backing") {
            PlatformNavigationBar(items = destinations(0), hasBackground = false)
        },
        ComponentPreviewCase("PlatformNavigationBar · Obscured") {
            PlatformNavigationBar(items = destinations(0), isObscured = true)
        },
        ComponentPreviewCase("PlatformTabBar · Disabled") {
            PlatformTabBar(
                labels = listOf("Image", "Info"),
                selectedIndex = 0,
                onSelect = {},
                isEnabled = false
            )
        },
        ComponentPreviewCase("MaterialTabBar · Disabled") {
            MaterialTabBar(
                labels = listOf("Image", "Info"),
                selectedIndex = 0,
                onSelect = {},
                isEnabled = false
            )
        },
        ComponentPreviewCase("PlatformTabBar · Long labels") {
            PlatformTabBar(
                labels = listOf("Original source image", "Generated image metadata"),
                selectedIndex = 1,
                onSelect = {})
        },
        ComponentPreviewCase("PlatformDrawerSheet") {
            PlatformDrawerSheet {
                Text(
                    "SDAI",
                    Modifier.padding(16.dp)
                ); PlatformDrawerItem(
                label = "Gallery",
                icon = Icons.Default.Settings,
                isSelected = true,
                onClick = {}); PlatformDrawerItem(
                label = "Settings",
                icon = Icons.Default.Settings,
                isSelected = false,
                onClick = {})
            }
        },
        ComponentPreviewCase("PlatformDrawerItem · Long selected") {
            PlatformDrawerItem(
                label = "A long destination name in the navigation drawer",
                icon = Icons.Default.Settings,
                isSelected = true,
                onClick = {})
        },
    ),
)

/**
 * Renders named navigation states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 500)
@Preview(name = "Dark", widthDp = 400, heightDp = 500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 500, fontScale = 1.5f)
@Composable
private fun NavigationPreview(@PreviewParameter(NavigationPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}

/**
 * Builds inert destination descriptors with one selected entry.
 *
 * @param selected Index highlighted by the native/Material navigation renderer.
 * @author Dmitriy Moroz
 */
private fun destinations(selected: Int): List<PlatformNavigationBarItem> =
    listOf("Generate", "Gallery", "Settings").mapIndexed { index, label ->
        PlatformNavigationBarItem(
            label,
            Icons.Default.Settings,
            index == selected,
            "Open $label",
            {})
    }
