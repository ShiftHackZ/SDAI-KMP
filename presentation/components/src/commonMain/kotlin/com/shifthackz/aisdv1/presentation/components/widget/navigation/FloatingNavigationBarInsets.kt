package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Whether persistent navigation floats over content instead of occupying an opaque bottom strip.
 *
 * @author Dmitriy Moroz
 */
expect val isPlatformNavigationBarFloating: Boolean

/**
 * Bottom clearance for the current floating navigation area, including its safe-area inset.
 * Scroll containers add it to content padding so the last item can scroll above the bar; fixed
 * actions reserve it in their layout while the screen itself continues underneath the bar.
 *
 * @author Dmitriy Moroz
 */
val LocalFloatingNavigationBarInset = staticCompositionLocalOf { 0.dp }
