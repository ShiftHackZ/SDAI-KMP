package com.shifthackz.aisdv1.presentation.components.widget.color

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.presentation.components.theme.global.catppuccinAccentColor
import com.shifthackz.aisdv1.presentation.components.theme.global.catppuccinBaseColor
import com.shifthackz.aisdv1.presentation.components.theme.global.catppuccinOverlayColor

/**
 * Shows the three supported dark palettes as selectable base-color swatches.
 *
 * @param modifier Layout modifiers for the palette selector.
 * @param selectedToken Palette currently marked as selected.
 * @param colorToken Accent used for the selection mark and border within each palette.
 * @param onSelected Receives the chosen palette's base color and palette identity.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun DarkThemeColorSelector(
    modifier: Modifier = Modifier,
    selectedToken: DarkThemeToken = DarkThemeToken.FRAPPE,
    colorToken: ColorToken = ColorToken.MAUVE,
    onSelected: (Color, DarkThemeToken) -> Unit = { _, _ -> },
) {
    Column(modifier = modifier) {
        val rowModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
        val colorModifier = Modifier
            .weight(1f)
            .size(32.dp)
        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            listOf(
                DarkThemeToken.FRAPPE,
                DarkThemeToken.MACCHIATO,
                DarkThemeToken.MOCHA,
            ).forEach { token ->
                val baseColor = catppuccinBaseColor(isDark = true, darkThemeToken = token)
                val accentColor =
                    catppuccinAccentColor(colorToken, isDark = true, darkThemeToken = token)
                ColorComposable(
                    modifier = colorModifier,
                    selectedToken = selectedToken,
                    token = token,
                    color = baseColor,
                    onClick = onSelected,
                    iconTint = accentColor,
                    selectedBorderTint = accentColor.copy(alpha = 0.4f),
                    unselectedBorderTint = catppuccinOverlayColor(
                        isDark = true,
                        darkThemeToken = token,
                    ).copy(alpha = 0.3f),
                    borderSize = 2.dp,
                )
            }
        }
    }
}
