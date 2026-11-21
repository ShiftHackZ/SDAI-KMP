@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Renders system Liquid Glass underneath shared Compose button content on iOS 26 and newer.
 * Native hit testing and accessibility are disabled; the enclosing Compose button owns both.
 * The resolved parent background fills the interop cutout while UIKit owns the glass edge and corners.
 * Standalone buttons use the screen background to avoid a rectangular surface-colored patch.
 *
 * @param isOutlined Selects regular secondary glass instead of prominent accent glass.
 * @param isEnabled Drives UIKit's disabled appearance.
 * @param isPressed Mirrors the Compose press interaction into UIKit highlighting.
 * @param shape Supplies the corner radius in the current layout direction and density.
 * @param modifier Bounds occupied by the decorative surface.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GlassButtonSurface(
    isOutlined: Boolean,
    isEnabled: Boolean,
    isPressed: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary.toUIColor()
    val backingColor = (LocalPlatformControlBackgroundColor.current
        ?: MaterialTheme.colorScheme.background).toUIColor()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    val outline = shape.createOutline(size.toSize(), layoutDirection, density)
    val cornerRadius = (outline as? Outline.Rounded)?.roundRect?.topLeftCornerRadius?.x ?: 0f
    val radiusPoints = cornerRadius / density.density
    UIKitView(
        modifier = modifier.onSizeChanged { size = it },
        factory = { NativeGlassButtonView() },
        update = { view ->
            view.update(
                isOutlined = isOutlined,
                isEnabled = isEnabled,
                isPressed = isPressed,
                isDark = isDark,
                accent = accent,
                backingColor = backingColor,
                cornerRadius = radiusPoints.toDouble(),
            )
        },
        properties = UIKitInteropProperties(
            interactionMode = null,
            isNativeAccessibilityEnabled = false
        ),
    )
}
