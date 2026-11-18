package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Returns a platform dynamic palette when requested and supported, otherwise leaves palette fallback to the caller.
 *
 * @param useDynamicColors Saved opt-in to platform-derived colors.
 * @param isDark Whether a dark palette is requested.
 *
 * @return Dynamic colors when available, otherwise null for application-palette fallback.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun dynamicColorSchemeOrNull(
    useDynamicColors: Boolean,
    isDark: Boolean,
): ColorScheme?
