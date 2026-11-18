package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Leaves UIKit bar coloring unchanged; iOS appearance is owned by the native host.
 *
 * @param colorScheme Theme palette whose surface provides the platform bar background.
 * @param isDark Whether system icons need the dark-theme contrast mode.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun SystemBarAppearance(
    colorScheme: ColorScheme,
    isDark: Boolean,
) {
    // iOS bar coloring is intentionally disabled for now. Updating UIKit window
    // colors from a Compose side effect can leave Compose Multiplatform with an
    // empty root view during launch on device/simulator.
}
