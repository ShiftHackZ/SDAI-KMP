package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds

/**
 * Uses the ordinary Compose clipping boundary for Material controls.
 * @param modifier Viewport placement and dimensions.
 * @param content Controls clipped to the viewport.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformClippedContent(modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier.clipToBounds()) { content() }
}
