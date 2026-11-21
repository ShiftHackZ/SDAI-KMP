package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Clips both Compose drawing and embedded native views to a bounded content viewport.
 * Use for moving native controls whose UIKit overlays otherwise escape a Compose clipping layer.
 *
 * @param modifier Viewport bounds supplied by the surrounding layout.
 * @param content Controls rendered inside the clipping boundary.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformClippedContent(modifier: Modifier = Modifier, content: @Composable () -> Unit)
