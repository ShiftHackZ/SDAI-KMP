package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Uses Compose control renderers inside noninteractive, density-scaled phone illustrations.
 * Native UIKit surfaces cannot follow all offscreen clipping and scaling of those illustrations.
 * Real screens keep native rendering, including the tutorial's own navigation buttons.
 *
 * @author Dmitriy Moroz
 */
val LocalPlatformControlPreview = staticCompositionLocalOf { false }
