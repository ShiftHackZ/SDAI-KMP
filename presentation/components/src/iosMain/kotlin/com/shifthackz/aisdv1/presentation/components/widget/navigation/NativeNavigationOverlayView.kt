@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIEvent
import platform.UIKit.UIView

/**
 * Keeps system tabs above scrolling Compose content without intercepting the rest of the app.
 * Its bounds match the navigation root, so control lookup uses the same coordinate space.
 *
 * @param containsControl Whether a point belongs to a visible native navigation control's hit region.
 * @author Dmitriy Moroz
 */
internal class NativeNavigationOverlayView(
    private val containsControl: (CValue<CGPoint>) -> Boolean,
) : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    /**
     * Forwards native destination touches to UIKit and lets all other touches reach sibling content.
     *
     * @param point Proposed touch position in navigation-root coordinates.
     * @param withEvent Event being routed through the native hierarchy.
     * @return UIKit's destination view, or null to continue hit testing the Compose sibling.
     * @author Dmitriy Moroz
     */
    override fun hitTest(point: CValue<CGPoint>, withEvent: UIEvent?): UIView? =
        if (containsControl(point)) super.hitTest(point, withEvent) else null
}
