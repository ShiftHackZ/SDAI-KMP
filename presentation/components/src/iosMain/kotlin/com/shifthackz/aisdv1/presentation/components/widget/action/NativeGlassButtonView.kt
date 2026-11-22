@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.ui.geometry.Rect
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectInset
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIButton
import platform.UIKit.UIButtonConfiguration
import platform.UIKit.UIButtonConfigurationCornerStyleFixed
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.UIBezierPath
import platform.UIKit.isAccessibilityElement
import platform.QuartzCore.CAShapeLayer
import platform.QuartzCore.CATransaction

/**
 * Fills the Compose interop cutout independently of the button's continuous glass outline.
 * A two-point inset keeps UIKit's outer glass highlight inside the embedded view's clipping bounds.
 * UIKit draws the glass edge without an additional Compose border or clipping curve.
 * This view is decorative; the enclosing Compose control owns input and accessibility.
 *
 * @author Dmitriy Moroz
 */
internal class NativeGlassButtonView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val button = UIButton(frame = CGRectMake(0.0, 0.0, 0.0, 0.0))
    private val viewportMask = CAShapeLayer()

    init {
        userInteractionEnabled = false
        isAccessibilityElement = false
        button.userInteractionEnabled = false
        button.isAccessibilityElement = false
        button.backgroundColor = UIColor.clearColor
        addSubview(button)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        button.setFrame(CGRectInset(bounds, 2.0, 2.0))
    }

    /**
     * Clips an overlay to the visible part of its Compose scroll viewport. UIKit overlays do not
     * inherit canvas clipping, so their native glyph and glass must share this explicit mask.
     *
     * @param bounds Visible rectangle relative to this view's origin, in UIKit points.
     * @author Dmitriy Moroz
     */
    fun updateVisibleBounds(bounds: Rect) {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        viewportMask.path = UIBezierPath.bezierPathWithRect(
            CGRectMake(
                bounds.left.toDouble(), bounds.top.toDouble(),
                bounds.width.coerceAtLeast(0f).toDouble(),
                bounds.height.coerceAtLeast(0f).toDouble(),
            )
        ).CGPath
        layer.mask = viewportMask
        CATransaction.commit()
    }

    /**
     * Applies the app theme and shared control state to the native glass renderer.
     * Regular glass provides a readable secondary surface in both light and dark appearances.
     *
     * @param isOutlined Whether this is a secondary rather than prominent accent action.
     * @param isEnabled Whether UIKit should draw its enabled appearance.
     * @param isPressed Whether the enclosing Compose control is currently pressed.
     * @param isDark App appearance, independent of the system appearance.
     * @param accent Tint for prominent glass.
     * @param backingColor Parent surface color filling the rectangular interop cutout.
     * @param cornerRadius Shared shape radius in UIKit points, rendered with UIKit's continuous curve.
     * @param image Optional native glyph when the entire circular control is above the Compose canvas.
     * @param foreground Tint of that native glyph; null retains the configuration's default foreground.
     * @author Dmitriy Moroz
     */
    fun update(
        isOutlined: Boolean,
        isEnabled: Boolean,
        isPressed: Boolean,
        isDark: Boolean,
        accent: UIColor,
        backingColor: UIColor,
        cornerRadius: Double,
        image: UIImage? = null,
        foreground: UIColor? = null,
    ) {
        backgroundColor = backingColor
        overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        button.tintColor = accent
        button.configuration = (if (isOutlined) {
            UIButtonConfiguration.glassButtonConfiguration()
        } else {
            UIButtonConfiguration.prominentGlassButtonConfiguration()
        }).apply {
            this.image = image
            if (foreground != null) baseForegroundColor = foreground
            cornerStyle = UIButtonConfigurationCornerStyleFixed
            background =
                background.apply { this.cornerRadius = (cornerRadius - 2.0).coerceAtLeast(0.0) }
        }
        button.enabled = isEnabled
        button.highlighted = isPressed
    }
}
