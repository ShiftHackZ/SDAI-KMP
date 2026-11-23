package com.shifthackz.aisdv1.presentation.components.preview.color

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.color.AccentColorSelector
import com.shifthackz.aisdv1.presentation.components.widget.color.ColorIconChip
import com.shifthackz.aisdv1.presentation.components.widget.color.DarkThemeColorSelector
import androidx.compose.ui.Modifier

/**
 * Palette selection and swatch states in light and dark themes with enabled and disabled chips.
 *
 * @author Dmitriy Moroz
 */
internal class ColorPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("AccentColorSelector · false · MAUVE") {
            AccentColorSelector(isDark = false, selectedToken = ColorToken.MAUVE)
        },
        ComponentPreviewCase("AccentColorSelector · false · BLUE") {
            AccentColorSelector(isDark = false, selectedToken = ColorToken.BLUE)
        },
        ComponentPreviewCase("AccentColorSelector · false · RED") {
            AccentColorSelector(isDark = false, selectedToken = ColorToken.RED)
        },
        ComponentPreviewCase("AccentColorSelector · true · MAUVE") {
            AccentColorSelector(isDark = true, selectedToken = ColorToken.MAUVE)
        },
        ComponentPreviewCase("AccentColorSelector · true · BLUE") {
            AccentColorSelector(isDark = true, selectedToken = ColorToken.BLUE)
        },
        ComponentPreviewCase("AccentColorSelector · true · RED") {
            AccentColorSelector(isDark = true, selectedToken = ColorToken.RED)
        },
        ComponentPreviewCase("DarkThemeColorSelector · FRAPPE") {
            DarkThemeColorSelector(selectedToken = DarkThemeToken.FRAPPE)
        },
        ComponentPreviewCase("DarkThemeColorSelector · MACCHIATO") {
            DarkThemeColorSelector(selectedToken = DarkThemeToken.MACCHIATO)
        },
        ComponentPreviewCase("DarkThemeColorSelector · MOCHA") {
            DarkThemeColorSelector(selectedToken = DarkThemeToken.MOCHA)
        },
        ComponentPreviewCase("ColorIconChip · true · true") {
            ColorIconChip(
                color = MaterialTheme.colorScheme.primary,
                isSelected = true,
                onClick = {},
                contentDescription = "Accent",
                isEnabled = true
            )
        },
        ComponentPreviewCase("ColorIconChip · true · false") {
            ColorIconChip(
                color = MaterialTheme.colorScheme.primary,
                isSelected = true,
                onClick = {},
                contentDescription = "Accent",
                isEnabled = false
            )
        },
        ComponentPreviewCase("ColorIconChip · false · true") {
            ColorIconChip(
                color = MaterialTheme.colorScheme.primary,
                isSelected = false,
                onClick = {},
                contentDescription = "Accent",
                isEnabled = true
            )
        },
        ComponentPreviewCase("ColorIconChip · false · false") {
            ColorIconChip(
                color = MaterialTheme.colorScheme.primary,
                isSelected = false,
                onClick = {},
                contentDescription = "Accent",
                isEnabled = false
            )
        },
    ),
)

/**
 * Renders named color states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 420)
@Preview(name = "Dark", widthDp = 400, heightDp = 420, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun ColorPreview(@PreviewParameter(ColorPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
