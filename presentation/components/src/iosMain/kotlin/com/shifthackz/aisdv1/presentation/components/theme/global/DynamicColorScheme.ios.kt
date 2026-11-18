package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Returns null on iOS so the shared theme uses its configured application palette.
 *
 * @param useDynamicColors Saved opt-in to platform-derived colors.
 * @param isDark Whether a dark palette is requested.
 * @return Dynamic colors when available, otherwise null for application-palette fallback.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun dynamicColorSchemeOrNull(
    useDynamicColors: Boolean,
    isDark: Boolean,
): ColorScheme? = null
