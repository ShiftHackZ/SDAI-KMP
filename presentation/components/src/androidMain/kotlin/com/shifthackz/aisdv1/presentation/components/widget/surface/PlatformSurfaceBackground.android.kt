package com.shifthackz.aisdv1.presentation.components.widget.surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/** Android retains the Material surface without native glass. @author Dmitriy Moroz */
@Composable
internal actual fun isPlatformSurfaceGlassAvailable(): Boolean = false

/**
 * Leaves background rendering to Material on Android.
 * @param color Shared tint rendered by the enclosing Material surface.
 * @param backingColor Parent canvas; no native cutout is required.
 * @param shape Clipping already applied by Material.
 * @param inset Native rim spacing, unused on Android.
 * @param isTopEdgeVisible Shared top-edge treatment, unused on Android.
 * @param isBottomEdgeVisible Shared bottom-edge treatment, unused on Android.
 * @param modifier Native backdrop bounds, unused on Android.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun PlatformSurfaceBackground(
    color: Color,
    backingColor: Color,
    shape: Shape,
    inset: Dp,
    isTopEdgeVisible: Boolean,
    isBottomEdgeVisible: Boolean,
    modifier: Modifier,
) = Unit
