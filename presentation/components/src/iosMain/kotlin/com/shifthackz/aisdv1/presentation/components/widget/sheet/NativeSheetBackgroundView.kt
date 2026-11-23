@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.sheet

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGColorGetAlpha
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle.UIBlurEffectStyleSystemUltraThinMaterial
import platform.UIKit.UIColor
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.UIVisualEffectView
import platform.UIKit.isAccessibilityElement

/**
 * Draws the drawer's ultra-thin system material and separate 20-percent theme tint.
 * Its native layer stays below embedded glass cards and controls; Compose clips the sheet outline.
 *
 * @author Dmitriy Moroz
 */
internal class NativeSheetBackgroundView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val backdrop = UIVisualEffectView(
        effect = UIBlurEffect.effectWithStyle(UIBlurEffectStyleSystemUltraThinMaterial),
    )
    private val tint = UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0))
    private var previousColor: UIColor? = null

    init {
        userInteractionEnabled = false
        isAccessibilityElement = false
        backdrop.userInteractionEnabled = false
        tint.userInteractionEnabled = false
        addSubview(backdrop)
        addSubview(tint)
    }

    override fun didMoveToWindow() {
        super.didMoveToWindow()
        superview?.superview?.layer?.zPosition = -2.0
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        backdrop.setFrame(bounds)
        tint.setFrame(bounds)
    }

    /**
     * Applies app appearance independently of the system and tints the fully opaque effect view.
     *
     * @param color Theme color mixed at the same strength as the navigation drawer.
     * @param isDark Whether the app currently uses a dark palette.
     *
     * @author Dmitriy Moroz
     */
    fun update(color: UIColor, isDark: Boolean) {
        val appearance = if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        if (overrideUserInterfaceStyle != appearance) overrideUserInterfaceStyle = appearance
        if (previousColor != color) {
            tint.backgroundColor =
                color.colorWithAlphaComponent(CGColorGetAlpha(color.CGColor) * 0.20)
            previousColor = color
        }
    }
}
