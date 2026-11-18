package com.shifthackz.aisdv1.presentation.components.theme.global

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Returns Android 12+ wallpaper-derived Material colors only when dynamic colors are enabled.
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
): ColorScheme? {
    if (!useDynamicColors || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return if (isDark) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
}
