@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectInset
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIButton
import platform.UIKit.UIButtonConfiguration
import platform.UIKit.UIButtonConfigurationCornerStyleCapsule
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UIFont
import platform.UIKit.UIFontTextStyleBody
import platform.UIKit.UIViewContentMode.UIViewContentModeScaleAspectFit
import platform.UIKit.UIUserInterfaceLayoutDirection.UIUserInterfaceLayoutDirectionRightToLeft
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.isAccessibilityElement

/**
 * Decorative native drawer destination. Its glass can sample the Compose sheet because the whole
 * label/icon/button is above the canvas, without an opaque rectangular interop backing.
 * Compose supplies activation and accessibility; older iOS uses a tinted native configuration.
 *
 * @author Dmitriy Moroz
 */
internal class NativeDrawerButtonView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val button = UIButton()
    private val iconView = UIImageView()
    private val titleView = UILabel()
    private val hasGlass =
        NSProcessInfo.processInfo.operatingSystemVersion.useContents { majorVersion >= 26 }

    init {
        userInteractionEnabled = false
        isAccessibilityElement = false
        button.userInteractionEnabled = false
        button.isAccessibilityElement = false
        iconView.contentMode = UIViewContentModeScaleAspectFit
        iconView.isAccessibilityElement = false
        titleView.font = UIFont.preferredFontForTextStyle(UIFontTextStyleBody)
        titleView.adjustsFontForContentSizeCategory = true
        titleView.isAccessibilityElement = false
        addSubview(button)
        addSubview(iconView)
        addSubview(titleView)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        button.setFrame(CGRectInset(bounds, 2.0, 2.0))
        val width = bounds.useContents { size.width }
        val height = bounds.useContents { size.height }
        val isRtl =
            effectiveUserInterfaceLayoutDirection == UIUserInterfaceLayoutDirectionRightToLeft
        iconView.setFrame(
            CGRectMake(
                if (isRtl) width - 44.0 else 20.0,
                (height - 24.0) / 2.0,
                24.0,
                24.0
            )
        )
        titleView.setFrame(
            CGRectMake(
                if (isRtl) 20.0 else 60.0,
                0.0,
                (width - 80.0).coerceAtLeast(0.0),
                height
            )
        )
    }

    /**
     * Refreshes native glass, content and app-specific appearance without changing row geometry.
     * Separate native label and icon retain theme contrast without configuration color transforms.
     *
     * @param label Localized destination name.
     * @param image Shared template icon.
     * @param isSelected Whether prominent accent glass indicates the current destination.
     * @param accent Selected app accent.
     * @param foreground Normal destination foreground.
     * @param selectedForeground Foreground with contrast against the accent.
     * @param isDark Manual or system app appearance.
     *
     * @author Dmitriy Moroz
     */
    fun update(
        label: String,
        image: UIImage?,
        isSelected: Boolean,
        accent: UIColor,
        foreground: UIColor,
        selectedForeground: UIColor,
        isDark: Boolean
    ) {
        overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        button.tintColor = accent
        val contentColor = if (isSelected) selectedForeground else foreground
        titleView.text = label
        titleView.textColor = contentColor
        iconView.image = image
        iconView.tintColor = contentColor
        button.configuration = (if (hasGlass) {
            if (isSelected) {
                UIButtonConfiguration.prominentGlassButtonConfiguration()
            } else {
                UIButtonConfiguration.glassButtonConfiguration()
            }
        } else {
            if (isSelected) {
                UIButtonConfiguration.filledButtonConfiguration()
            } else {
                UIButtonConfiguration.tintedButtonConfiguration()
            }
        }).apply {
            cornerStyle = UIButtonConfigurationCornerStyleCapsule
            if (isSelected) baseBackgroundColor = accent
        }
    }
}
