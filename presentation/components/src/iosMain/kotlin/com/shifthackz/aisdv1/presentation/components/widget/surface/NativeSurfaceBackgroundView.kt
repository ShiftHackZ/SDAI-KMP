@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.surface

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
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
        val top = if (isTopEdgeVisible) inset else -16.0
        val bottom = if (isBottomEdgeVisible) inset else -16.0
        bounds.useContents {
            effectView.setFrame(
                CGRectMake(
                    inset, top,
                    (size.width - 2 * inset).coerceAtLeast(0.0),
                    (size.height - top - bottom).coerceAtLeast(0.0),
                )
            )
        }
    }

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
     * @param topLeftRadius Outer top-left corner radius in logical points.
     * @param topRightRadius Outer top-right corner radius in logical points.
     * @param bottomLeftRadius Outer bottom-left corner radius in logical points.
     * @param bottomRightRadius Outer bottom-right corner radius in logical points.
     *
     * @author Dmitriy Moroz
     */
    fun update(
        isDark: Boolean, backingColor: UIColor, color: UIColor, inset: Double,
        isTopEdgeVisible: Boolean, isBottomEdgeVisible: Boolean,
        topLeftRadius: Double, topRightRadius: Double,
        bottomLeftRadius: Double, bottomRightRadius: Double,
    ) {
        this.inset = inset
        this.isTopEdgeVisible = isTopEdgeVisible
        this.isBottomEdgeVisible = isBottomEdgeVisible
        effectView.cornerConfiguration = UICornerConfiguration.configurationWithTopLeftRadius(
            UICornerRadius.fixedRadius((topLeftRadius - inset).coerceAtLeast(0.0)),
            UICornerRadius.fixedRadius((topRightRadius - inset).coerceAtLeast(0.0)),
            UICornerRadius.fixedRadius((bottomLeftRadius - inset).coerceAtLeast(0.0)),
            UICornerRadius.fixedRadius((bottomRightRadius - inset).coerceAtLeast(0.0)),
        )
        setNeedsLayout()
        backgroundColor = backingColor
        overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        if (previousColor != color) {
            effectView.effect = UIGlassEffect.effectWithStyle(UIGlassEffectStyleRegular).apply {
                tintColor = color
            }
            previousColor = color
        }
    }
}
