package com.shifthackz.aisdv1.presentation.components.widget.color

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws a clickable circular swatch with a border and checkmark for the selected token.
 *
 * @param modifier Size and placement of the swatch.
 * @param selectedToken Caller-owned selection used for equality comparison.
 * @param token Identity represented by this swatch.
 * @param color Swatch fill color returned on activation.
 * @param onClick Receives this swatch's fill color and token.
 * @param iconTint Foreground color of the selected checkmark.
 * @param selectedBorderTint Border color when this token is selected.
 * @param unselectedBorderTint Border color when another token is selected.
 * @param borderSize Thickness reserved around the inner swatch fill.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun <T : Any> ColorComposable(
    modifier: Modifier = Modifier,
    selectedToken: T,
    token: T,
    color: Color,
    onClick: (Color, T) -> Unit,
    iconTint: Color,
    selectedBorderTint: Color = color.copy(alpha = 0.4f),
    unselectedBorderTint: Color = Color.Transparent,
    borderSize: Dp = 4.dp,
) {
    val selected = token == selectedToken
    Box(
        modifier = modifier
            .background(
                color = if (selected) selectedBorderTint else unselectedBorderTint,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .clickable { onClick(color, token) },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(borderSize)
                .size(1000.dp)
                .background(color = color, shape = CircleShape)
                .clip(CircleShape)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = iconTint,
            )
        }
    }
}
