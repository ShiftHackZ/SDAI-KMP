package com.shifthackz.aisdv1.presentation.theme.global

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color

/**
 * Material values last committed by a visible retained home tab. The palette is a copy of the
 * root colors, so a hidden tab never observes subsequent changes through a shared palette.
 *
 * @param colorScheme Independent copy of all palette values at the visible composition.
 * @param typography Text styles selected for that appearance.
 * @param shapes Material corner shapes selected for that appearance.
 * @param contentColor Foreground color inherited from the root surface at that composition.
 * @author Dmitriy Moroz
 */
internal data class HomeRouteThemeSnapshot(
    val colorScheme: ColorScheme,
    val typography: Typography,
    val shapes: Shapes,
    val contentColor: Color,
)
