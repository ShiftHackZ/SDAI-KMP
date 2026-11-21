@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.input

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISwitch
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.isAccessibilityElement

/**
 * Fits a system switch proportionally inside the shared touch area, leaving room for its glass thumb.
 * The Compose owner supplies state and releases the callback when the native view leaves composition.
 *
 * @property onValueChange Receives user-requested values while this view is attached to Compose.
 * @author Dmitriy Moroz
 */
internal class NativeSwitchView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val control = UISwitch()
    var onValueChange: ((Boolean) -> Unit)? = null

    init {
        isAccessibilityElement = false
        control.isAccessibilityElement = false
        addSubview(control)
        control.addTarget(this, NSSelectorFromString("valueChanged:"), UIControlEventValueChanged)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        control.transform = CGAffineTransformMakeScale(1.0, 1.0)
        control.sizeToFit()
        val availableWidth = bounds.useContents { (size.width - 4.0).coerceAtLeast(0.0) }
        val nativeWidth = control.intrinsicContentSize.useContents { width }
        val scale = minOf(1.0, availableWidth / nativeWidth)
        control.transform = CGAffineTransformMakeScale(scale, scale)
        control.center = bounds.useContents { CGPointMake(size.width / 2, size.height / 2) }
    }

    /**
     * Updates UIKit from shared state without generating another value-change event.
     *
     * @param isChecked Current shared value.
     * @param isEnabled Whether the control is enabled.
     * @param isDark App appearance, including a manual theme override.
     * @param accent Selected app accent for the enabled track.
     * @param backingColor Resolved Compose container color, filling transparent native regions.
     * @param onCheckedChange Requested-value callback; null leaves activation to the parent row.
     * @author Dmitriy Moroz
     */
    fun update(
        isChecked: Boolean,
        isEnabled: Boolean,
        isDark: Boolean,
        accent: UIColor,
        backingColor: UIColor,
        onCheckedChange: ((Boolean) -> Unit)?,
    ) {
        onValueChange = onCheckedChange
        backgroundColor = backingColor
        control.enabled = isEnabled
        control.onTintColor = accent
        control.overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        if (control.on != isChecked) control.setOn(isChecked, animated = window != null)
    }

    /**
     * Forwards one UIKit value change to the current shared-state owner.
     *
     * @param sender The native switch after the user's interaction.
     * @author Dmitriy Moroz
     */
    @ObjCAction
    fun valueChanged(sender: UISwitch) {
        onValueChange?.invoke(sender.on)
    }
}
