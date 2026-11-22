package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether native controls in a retained screen may draw above the Compose canvas.
 * A host covering that screen with another destination disables its native overlays while
 * retaining the screen's layout, state and effects. Active-tab visibility remains independent.
 *
 * @author Dmitriy Moroz
 */
val LocalPlatformControlVisibility = staticCompositionLocalOf { true }
