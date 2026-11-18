package com.shifthackz.aisdv1.presentation.components.theme

import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Shared slider colors suppressing tick marks while retaining Material theme track and thumb roles.
 *
 * @author Dmitriy Moroz
 */
val sliderColors: SliderColors
    @Composable get() = SliderDefaults.colors(
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
