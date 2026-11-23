package com.shifthackz.aisdv1.presentation.components.widget.surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/**
 * Keeps background drawing in Compose because Android uses the Material surface renderer.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun isPlatformSurfaceGlassAvailable(): Boolean = false

/**
 * Leaves background rendering to Material on Android.
 *
 * @param color Shared tint rendered by the enclosing Material surface.
 * @param backingColor Parent canvas; no native cutout is required.
 * @param shape Clipping already applied by Material.
 * @param inset Native rim spacing, unused on Android.
 * @param isTopEdgeVisible Shared top-edge treatment, unused on Android.
 * @param isBottomEdgeVisible Shared bottom-edge treatment, unused on Android.
 * @param shouldClipBackground Native-only backing clipping; Material already clips its surface.
 * @param modifier Native backdrop bounds, unused on Android.
 *
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
    shouldClipBackground: Boolean,
    modifier: Modifier,
) = Unit
