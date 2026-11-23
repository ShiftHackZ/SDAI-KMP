@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.surface

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.QuartzCore.CATransaction
import platform.UIKit.UIColor
import platform.UIKit.UICornerConfiguration
import platform.UIKit.UICornerRadius
import platform.UIKit.UIGlassEffect
import platform.UIKit.UIGlassEffectStyle.UIGlassEffectStyleRegular
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.UIVisualEffectView
import platform.UIKit.isAccessibilityElement

/**
 * Draws a tinted Liquid Glass surface over the app's resolved parent color.
 * The configured inset keeps the native rim inside Compose's clipping bounds.
 * Optional native container clipping follows the outer outline when placed over another material.
 * A lower native layer keeps embedded switches above the backdrop when theme updates reorder views.
 * Compose retains ownership of the surface's input, text and accessibility.
 *
 * @author Dmitriy Moroz
 */
internal class NativeSurfaceBackgroundView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val effectView = UIVisualEffectView(effect = null)
    private var previousColor: UIColor? = null
    private var inset = 2.0
    private var isTopEdgeVisible = true
    private var isBottomEdgeVisible = true
    private var shouldClipBackground = false
    private var shape: Shape = RoundedCornerShape(0.dp)
    private var density: Density = Density(1f)
    private var layoutDirection = LayoutDirection.Ltr
    private var previousWidth = -1.0
    private var previousHeight = -1.0
    private var shouldUpdateGeometry = true

    init {
        userInteractionEnabled = false
        isAccessibilityElement = false
        effectView.userInteractionEnabled = false
        addSubview(effectView)
    }

    override fun didMoveToWindow() {
        super.didMoveToWindow()
        // The native view sits in a positioning view inside Compose's clipping wrapper.
        superview?.superview?.layer?.zPosition = -1.0
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        val width = bounds.useContents { size.width }
        val height = bounds.useContents { size.height }
        if (!shouldUpdateGeometry && width == previousWidth && height == previousHeight) return
        previousWidth = width
        previousHeight = height
        shouldUpdateGeometry = false
        val scale = density.density
        val outline = shape.createOutline(
            Size((width * scale).toFloat(), (height * scale).toFloat()), layoutDirection, density,
        )
        val corners = (outline as? Outline.Rounded)?.roundRect
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        cornerConfiguration =
            createCornerConfiguration(if (shouldClipBackground) corners else null, scale, 0.0)
        clipsToBounds = shouldClipBackground
        effectView.cornerConfiguration = createCornerConfiguration(corners, scale, inset)
        val top = if (isTopEdgeVisible) inset else -16.0
        val bottom = if (isBottomEdgeVisible) inset else -16.0
        effectView.setFrame(
            CGRectMake(
                inset, top,
                (width - 2 * inset).coerceAtLeast(0.0),
                (height - top - bottom).coerceAtLeast(0.0),
            )
        )
        CATransaction.commit()
    }

    /**
     * Converts the shared outer outline into native radii for the container or its inset effect.
     *
     * @param corners Rounded outline; null represents square corners.
     * @param scale Compose pixels per UIKit point.
     * @param inset Distance from the outer outline to the native effect, in points.
     *
     * @author Dmitriy Moroz
     */
    private fun createCornerConfiguration(
        corners: RoundRect?,
        scale: Float,
        inset: Double,
    ): UICornerConfiguration = UICornerConfiguration.configurationWithTopLeftRadius(
        UICornerRadius.fixedRadius(
            ((corners?.topLeftCornerRadius?.x ?: 0f) / scale - inset).coerceAtLeast(0.0)
        ),
        UICornerRadius.fixedRadius(
            ((corners?.topRightCornerRadius?.x ?: 0f) / scale - inset).coerceAtLeast(0.0)
        ),
        UICornerRadius.fixedRadius(
            ((corners?.bottomLeftCornerRadius?.x ?: 0f) / scale - inset).coerceAtLeast(0.0)
        ),
        UICornerRadius.fixedRadius(
            ((corners?.bottomRightCornerRadius?.x ?: 0f) / scale - inset).coerceAtLeast(0.0)
        ),
    )

    /**
     * Fills the native cutout with the enclosing canvas color and applies the surface tint to glass.
     * Reuses the effect until its tint changes, including during animated row highlights.
     *
     * @param isDark App appearance, independent of the system preference.
     * @param backingColor Enclosing canvas color; may be clear when another native surface supplies it.
     * @param color Themed tint, preserving its configured alpha.
     * @param inset Space retaining the native glass rim inside the clipping bounds.
     * @param isTopEdgeVisible Whether this segment starts the surface; false clips away the top rim.
     * @param isBottomEdgeVisible Whether this segment ends the surface; false clips away the bottom rim.
     * @param shape Corner outline resolved from actual UIKit bounds during layout.
     * @param density Converts between Compose pixels and UIKit points.
     * @param layoutDirection Resolves asymmetric start/end corners for the active locale.
     * @param shouldClipBackground Whether the complete backing is clipped to the shared outer shape.
     *
     * @author Dmitriy Moroz
     */
    fun update(
        isDark: Boolean, backingColor: UIColor, color: UIColor, inset: Double,
        isTopEdgeVisible: Boolean, isBottomEdgeVisible: Boolean,
        shape: Shape, density: Density, layoutDirection: LayoutDirection,
        shouldClipBackground: Boolean,
    ) {
        val hasGeometryChanged = this.inset != inset || this.isTopEdgeVisible != isTopEdgeVisible ||
                this.isBottomEdgeVisible != isBottomEdgeVisible || this.shape != shape ||
                this.density != density || this.layoutDirection != layoutDirection ||
                this.shouldClipBackground != shouldClipBackground
        this.inset = inset
        this.isTopEdgeVisible = isTopEdgeVisible
        this.isBottomEdgeVisible = isBottomEdgeVisible
        this.shape = shape
        this.density = density
        this.layoutDirection = layoutDirection
        this.shouldClipBackground = shouldClipBackground
        if (hasGeometryChanged) {
            shouldUpdateGeometry = true
            setNeedsLayout()
        }
        if (backgroundColor != backingColor) backgroundColor = backingColor
        val appearance = if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        if (overrideUserInterfaceStyle != appearance) overrideUserInterfaceStyle = appearance
        if (previousColor != color) {
            effectView.effect = UIGlassEffect.effectWithStyle(UIGlassEffectStyleRegular).apply {
                tintColor = color
            }
            previousColor = color
        }
    }
}
