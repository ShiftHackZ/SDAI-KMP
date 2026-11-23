package com.shifthackz.aisdv1.presentation.components.preview.item

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.Supporter
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.item.GridIcon
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsHeader
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsHeaderContent
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItemContent
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsList
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsToggleItem
import com.shifthackz.aisdv1.presentation.components.widget.item.SupporterItem
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.source.BetaBadge

/**
 * Settings rows and headers, loading and toggle states, supporters and every gallery grid icon.
 *
 * @author Dmitriy Moroz
 */
internal class ItemPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("SettingsItem · Plain") {
            SettingsItem(text = "Configuration".asUiText())
        },
        ComponentPreviewCase("SettingsItem · Icon") {
            SettingsItem(text = "Configuration".asUiText(), startIcon = Icons.Default.Settings)
        },
        ComponentPreviewCase("SettingsItem · Loading") {
            SettingsItem(text = "Configuration".asUiText(), loading = true)
        },
        ComponentPreviewCase("SettingsItem · Disabled") {
            SettingsItem(text = "Configuration".asUiText(), enabled = false)
        },
        ComponentPreviewCase("SettingsItem · Selected") {
            SettingsItem(text = "Configuration".asUiText(), selected = true)
        },
        ComponentPreviewCase("SettingsItem · Value") {
            SettingsItem(text = "Configuration".asUiText(), endValueText = "Enabled".asUiText())
        },
        ComponentPreviewCase("SettingsItem · Long content") {
            SettingsItem(
                text = "A long configuration setting name that wraps on narrow screens".asUiText(),
                endValueText = "A long descriptive value".asUiText(),
            )
        },
        ComponentPreviewCase("SettingsItem · Custom slots") {
            SettingsItem(
                text = "Configuration".asUiText(),
                startIconContent = { Icon(Icons.Default.Settings, null) },
                textSuffixContent = { BetaBadge() },
                endValueContent = { Text("42") },
            )
        },
        ComponentPreviewCase("SettingsItem · No chevron") {
            SettingsItem(text = "Configuration".asUiText(), showChevron = false)
        },
        ComponentPreviewCase("SettingsItem · String overload") {
            SettingsItem(text = "Configuration", endValueText = "Saved")
        },
        ComponentPreviewCase("SettingsItemContent · UiText") {
            SettingsItemContent(text = "Configuration".asUiText(), icon = Icons.Default.Settings)
        },
        ComponentPreviewCase("SettingsItemContent · String") {
            SettingsItemContent(text = "Configuration", textSuffixContent = { BetaBadge() })
        },
        ComponentPreviewCase("SettingsToggleItem · true · Enabled true") {
            SettingsToggleItem(
                text = "Use cache".asUiText(),
                isChecked = true,
                onCheckedChange = {},
                isEnabled = true,
                startIcon = Icons.Default.Settings
            )
        },
        ComponentPreviewCase("SettingsToggleItem · true · Enabled false") {
            SettingsToggleItem(
                text = "Use cache".asUiText(),
                isChecked = true,
                onCheckedChange = {},
                isEnabled = false,
                startIcon = Icons.Default.Settings
            )
        },
        ComponentPreviewCase("SettingsToggleItem · false · Enabled true") {
            SettingsToggleItem(
                text = "Use cache".asUiText(),
                isChecked = false,
                onCheckedChange = {},
                isEnabled = true,
                startIcon = Icons.Default.Settings
            )
        },
        ComponentPreviewCase("SettingsToggleItem · false · Enabled false") {
            SettingsToggleItem(
                text = "Use cache".asUiText(),
                isChecked = false,
                onCheckedChange = {},
                isEnabled = false,
                startIcon = Icons.Default.Settings
            )
        },
        ComponentPreviewCase("SettingsToggleItem · Loading") {
            SettingsToggleItem(
                text = "Use cache".asUiText(),
                isChecked = true,
                onCheckedChange = {},
                isLoading = true
            )
        },
        ComponentPreviewCase("SettingsHeader · Loading false") {
            SettingsHeader(text = "General".asUiText(), loading = false)
        },
        ComponentPreviewCase("SettingsHeaderContent · Loading false") {
            SettingsHeaderContent(text = "General", loading = false)
        },
        ComponentPreviewCase("SettingsHeader · Loading true") {
            SettingsHeader(text = "General".asUiText(), loading = true)
        },
        ComponentPreviewCase("SettingsHeaderContent · Loading true") {
            SettingsHeaderContent(text = "General", loading = true)
        },
        ComponentPreviewCase("SettingsList") {
            SettingsList(Modifier.height(300.dp)) {
                item { SettingsHeaderContent("General") }; items(
                4
            ) { SettingsItem(text = "Setting ${it + 1}") }
            }
        },
        ComponentPreviewCase("SupporterItem · Placeholder") {
            SupporterItem()
        },
        ComponentPreviewCase("SupporterItem · Named") {
            SupporterItem(supporter = Supporter(1, "Alex", "2026-11-20", "Thank you!"))
        },
        ComponentPreviewCase("SupporterItem · No message") {
            SupporterItem(supporter = Supporter(1, "Alex", "2026-11-20", ""))
        },
        ComponentPreviewCase("SupporterItem · Long text") {
            SupporterItem(
                supporter = Supporter(
                    1,
                    "A supporter with a long display name",
                    "invalid-date",
                    "Thank you for building this application. ".repeat(8)
                )
            )
        },
        ComponentPreviewCase("GridIcon · Fixed2") {
            GridIcon(grid = Grid.Fixed2)
        },
        ComponentPreviewCase("GridIcon · Fixed3") {
            GridIcon(grid = Grid.Fixed3)
        },
        ComponentPreviewCase("GridIcon · Fixed4") {
            GridIcon(grid = Grid.Fixed4)
        },
        ComponentPreviewCase("GridIcon · Fixed5") {
            GridIcon(grid = Grid.Fixed5)
        },
    ),
)

/**
 * Renders named item states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 500)
@Preview(name = "Dark", widthDp = 400, heightDp = 500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 500, fontScale = 1.5f)
@Composable
private fun ItemPreview(@PreviewParameter(ItemPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
