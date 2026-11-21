@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.CoreGraphics.CGRectInset
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSSelectorFromString
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSFontAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIControlStateSelected
import platform.UIKit.UIFont
import platform.UIKit.UIFontTextStyleSubheadline
import platform.UIKit.UISegmentedControl
import platform.UIKit.UISemanticContentAttributeForceLeftToRight
import platform.UIKit.UISemanticContentAttributeForceRightToLeft
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.isAccessibilityElement

/**
 * Renders peer pages with a system segmented control, retaining UIKit's gestures and accessibility.
 * Labels and selection are refreshed from Compose without emitting another selection event.
 *
 * @property onSelect Current page request callback, cleared when the Compose owner releases the view.
 * @author Dmitriy Moroz
 */
internal class NativeTabBarView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val control = UISegmentedControl()
    var onSelect: ((Int) -> Unit)? = null

    init {
        isAccessibilityElement = false
        control.addTarget(
            this,
            NSSelectorFromString("selectionChanged:"),
            UIControlEventValueChanged
        )
        addSubview(control)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        control.setFrame(CGRectInset(bounds, 2.0, 2.0))
    }

    /**
     * Synchronizes titles, state and app colors while preserving the system control's styling.
     *
     * @param labels Nonempty ordered localized segment titles.
     * @param selectedIndex Caller-owned selected page.
     * @param isEnabled Whether the control accepts page changes.
     * @param isDark Manual app appearance.
     * @param isRtl Whether segment ordering follows a right-to-left locale.
     * @param accent App accent for the selected segment.
     * @param selectedForeground Text contrasting with the selected accent.
     * @param foreground Text for unselected pages.
     * @param trackColor App surface behind unselected segments.
     * @param backingColor Parent canvas color, or clear within a native material host.
     * @param onSelect Requests a page change from the shared owner.
     * @author Dmitriy Moroz
     */
    fun update(
        labels: List<String>,
        selectedIndex: Int,
        isEnabled: Boolean,
        isDark: Boolean,
        isRtl: Boolean,
        accent: UIColor,
        selectedForeground: UIColor,
        foreground: UIColor,
        trackColor: UIColor,
        backingColor: UIColor,
        onSelect: (Int) -> Unit,
    ) {
        this.onSelect = onSelect
        backgroundColor = backingColor
        overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        control.semanticContentAttribute = if (isRtl) UISemanticContentAttributeForceRightToLeft
        else UISemanticContentAttributeForceLeftToRight
        if (control.numberOfSegments.toInt() != labels.size) {
            control.removeAllSegments()
            labels.forEachIndexed { index, label ->
                control.insertSegmentWithTitle(label, index.toULong(), false)
            }
        } else {
            labels.forEachIndexed { index, label -> control.setTitle(label, index.toULong()) }
        }
        control.enabled = isEnabled
        control.tintColor = accent
        control.selectedSegmentTintColor = accent
        control.backgroundColor = trackColor
        val font = UIFont.preferredFontForTextStyle(UIFontTextStyleSubheadline)
        control.setTitleTextAttributes(
            mapOf(
                NSForegroundColorAttributeName to foreground,
                NSFontAttributeName to font
            ), UIControlStateNormal
        )
        control.setTitleTextAttributes(
            mapOf(
                NSForegroundColorAttributeName to selectedForeground,
                NSFontAttributeName to font
            ), UIControlStateSelected
        )
        control.selectedSegmentIndex = selectedIndex.toLong()
    }

    /**
     * Forwards a user selection; programmatic state synchronization does not call this action.
     *
     * @param sender System control containing the newly selected segment.
     * @author Dmitriy Moroz
     */
    @ObjCAction
    fun selectionChanged(sender: UISegmentedControl) {
        if (sender.enabled) onSelect?.invoke(sender.selectedSegmentIndex.toInt())
    }
}
