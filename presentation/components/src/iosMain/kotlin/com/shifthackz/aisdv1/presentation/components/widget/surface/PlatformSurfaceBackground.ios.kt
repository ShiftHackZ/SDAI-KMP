@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.surface

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

/** Restricts glass to supported systems and visible, unscaled compositions. @author Dmitriy Moroz */
@Composable
internal actual fun isPlatformSurfaceGlassAvailable(): Boolean {
    val hasGlassSupport = remember {
        NSProcessInfo.processInfo.operatingSystemVersion.useContents { majorVersion >= 26 }
    }
    return hasGlassSupport && !LocalPlatformControlPreview.current &&
            LocalMviVisibility.current && LocalPlatformControlVisibility.current
}

/**
 * Places native glass beneath the shared surface without owning focus, input or accessibility.
 *
 * @param color Resolved themed tint.
 * @param backingColor Parent canvas filling the interop cutout.
 * @param shape Outer outline resolved for native corner radii.
 * @param inset Native rim spacing in logical units.
 * @param isTopEdgeVisible Whether this segment starts a glass surface.
 * @param isBottomEdgeVisible Whether this segment ends a glass surface.
 * @param modifier Surface backdrop bounds.
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
) {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    val outline = shape.createOutline(size.toSize(), direction, density)
    val corners = (outline as? Outline.Rounded)?.roundRect
    val scale = density.density.toDouble()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    UIKitView(
        modifier = modifier.onSizeChanged { size = it },
        factory = { NativeSurfaceBackgroundView() },
        update = {
            it.update(
                isDark = isDark,
                backingColor = backingColor.toUIColor(),
                color = color.toUIColor(),
                inset = inset.value.toDouble(),
                isTopEdgeVisible = isTopEdgeVisible,
                isBottomEdgeVisible = isBottomEdgeVisible,
                topLeftRadius = (corners?.topLeftCornerRadius?.x ?: 0f) / scale,
                topRightRadius = (corners?.topRightCornerRadius?.x ?: 0f) / scale,
                bottomLeftRadius = (corners?.bottomLeftCornerRadius?.x ?: 0f) / scale,
                bottomRightRadius = (corners?.bottomRightCornerRadius?.x ?: 0f) / scale,
            )
        },
        properties = UIKitInteropProperties(
            interactionMode = null,
            isNativeAccessibilityEnabled = false
        ),
    )
}
