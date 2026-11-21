package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Carries the resolved background behind embedded native controls so transparent UIKit regions
 * blend with their Compose container. Null uses the app's screen background for standalone controls.
 *
 * @author Dmitriy Moroz
 */
val LocalPlatformControlBackgroundColor = staticCompositionLocalOf<Color?> { null }
