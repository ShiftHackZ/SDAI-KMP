package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Applies platform system-bar appearance from the current shared theme where supported.
 *
 * @param colorScheme Theme palette whose surface provides the platform bar background.
 * @param isDark Whether system icons need the dark-theme contrast mode.
 *
 * @author Dmitriy Moroz
 */
@Composable
expect fun SystemBarAppearance(
    colorScheme: ColorScheme,
    isDark: Boolean,
)
