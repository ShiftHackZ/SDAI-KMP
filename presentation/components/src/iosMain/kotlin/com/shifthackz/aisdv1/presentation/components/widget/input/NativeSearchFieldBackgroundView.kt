@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.input

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectInset
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UICornerConfiguration
import platform.UIKit.UIGlassEffect
import platform.UIKit.UIGlassEffectStyle.UIGlassEffectStyleRegular
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.UIVisualEffectView
import platform.UIKit.isAccessibilityElement
import platform.UIKit.cornerConfiguration

/**
 * Supplies UIKit's native glass capsule without owning the search editor or its input.
 * The inset retains the outer highlight inside the interop bounds, matching circular toolbar actions.
 *
 * @author Dmitriy Moroz
 */
internal class NativeSearchFieldBackgroundView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val effect =
        UIVisualEffectView(UIGlassEffect.effectWithStyle(UIGlassEffectStyleRegular))

    init {
        userInteractionEnabled = false
        isAccessibilityElement = false
        effect.userInteractionEnabled = false
        effect.cornerConfiguration = UICornerConfiguration.capsuleConfiguration()
        addSubview(effect)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        effect.setFrame(CGRectInset(bounds, 2.0, 2.0))
    }

    /**
     * Matches manual app appearance and fills the interop cutout when the parent canvas is opaque.
     *
     * @param isDark Whether the app uses its dark appearance.
     * @param backingColor Resolved parent color, or clear inside an existing native material host.
     * @author Dmitriy Moroz
     */
    fun update(isDark: Boolean, backingColor: UIColor) {
        backgroundColor = backingColor
        overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
    }
}
