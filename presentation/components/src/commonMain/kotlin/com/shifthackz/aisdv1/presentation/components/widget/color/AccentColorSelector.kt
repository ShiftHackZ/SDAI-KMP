package com.shifthackz.aisdv1.presentation.components.widget.color

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.presentation.components.theme.global.catppuccinAccentColor
import com.shifthackz.aisdv1.presentation.components.theme.global.catppuccinBaseColor

/**
 * Shows all supported accent tokens in two rows of selectable platform color chips.
 *
 * @param modifier Layout modifiers for the accent swatch grid.
 * @param isDark Whether swatches use a dark palette.
 * @param darkThemeToken Dark palette used when dark appearance is active.
 * @param selectedToken Accent currently marked as selected.
 * @param onSelected Receives the chosen swatch color and its persistent accent token.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun AccentColorSelector(
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    darkThemeToken: DarkThemeToken = DarkThemeToken.FRAPPE,
    selectedToken: ColorToken = ColorToken.MAUVE,
    onSelected: (Color, ColorToken) -> Unit = { _, _ -> },
) {
    Column(modifier = modifier.selectableGroup()) {
        val iconTint = catppuccinBaseColor(isDark, darkThemeToken)
        ColorToken.entries.chunked(7).forEach { tokens ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tokens.forEach { token ->
                    val color = catppuccinAccentColor(token, isDark, darkThemeToken)
                    ColorIconChip(
                        modifier = Modifier.weight(1f),
                        color = color,
                        isSelected = token == selectedToken,
                        onClick = { onSelected(color, token) },
                        contentDescription = token.name.lowercase()
                            .replaceFirstChar { it.titlecase() },
                        iconTint = iconTint,
                    )
                }
            }
        }
    }
}
