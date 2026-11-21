@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIColor
import platform.UIKit.UIGestureRecognizerStateBegan
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UIGestureRecognizerStateCancelled
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UIImage
import platform.UIKit.UILongPressGestureRecognizer
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UIAccessibilityCustomAction
import platform.UIKit.accessibilityCustomActions
import com.shifthackz.aisdv1.core.localization.Localization
import platform.UIKit.UISemanticContentAttributeForceLeftToRight
import platform.UIKit.UISemanticContentAttributeForceRightToLeft
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import kotlin.math.floor

/**
 * Hosts a native tab bar with caller-owned selection and one long-press recognizer for shared help.
 * UIKit owns the entire control rendering and normal activation; no custom glass layers are drawn.
 *
 * @author Dmitriy Moroz
 */
internal class NativeNavigationBarView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)),
    UITabBarDelegateProtocol, UIGestureRecognizerDelegateProtocol {
    private val bar = UITabBar()
    private var items: List<PlatformNavigationBarItem> = emptyList()
    private var images: List<UIImage?> = emptyList()
    private var onLongPress: ((Int) -> Unit)? = null
    private var isRtl = false
    private var onHeightChanged: ((Double) -> Unit)? = null
    private var isHandlingLongPress = false
    private val helpGesture = UILongPressGestureRecognizer(this, NSSelectorFromString("showHelp:"))

    init {
        addSubview(bar)
        bar.delegate = this
        helpGesture.delegate = this
        bar.addGestureRecognizer(helpGesture)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        bar.setFrame(bounds)
        val width = bounds.useContents { size.width }
        val height = bounds.useContents { size.height }
        val preferredHeight =
            bar.sizeThatFits(CGSizeMake(width, height)).useContents { this.height }
        if (preferredHeight > 0 && preferredHeight != height) onHeightChanged?.invoke(
            preferredHeight
        )
    }

    override fun safeAreaInsetsDidChange() {
        super.safeAreaInsetsDidChange()
        setNeedsLayout()
    }

    /**
     * Updates native appearance, destinations and selection without invoking navigation callbacks.
     *
     * @param items Current localized destinations and callbacks.
     * @param images Native template images corresponding to the shared destination vectors.
     * @param isDark App appearance, independent of system appearance.
     * @param isRtl Whether the shared layout places the first destination at the right edge.
     * @param accent Selected icon and label tint.
     * @param foreground Unselected icon and label tint.
     * @param backingColor Resolved Compose surface behind the native control.
     * @param hasOpaqueBacking Whether the native view is under Compose and needs an opaque cutout.
     * @param onHeightChanged Receives UIKit's preferred total height, including its own safe area.
     * @param onLongPress Opens shared help for a destination without selecting it.
     * @author Dmitriy Moroz
     */
    fun update(
        items: List<PlatformNavigationBarItem>,
        images: List<UIImage?>,
        isDark: Boolean,
        isRtl: Boolean,
        accent: UIColor,
        foreground: UIColor,
        backingColor: UIColor,
        hasOpaqueBacking: Boolean,
        onHeightChanged: (Double) -> Unit,
        onLongPress: (Int) -> Unit,
    ) {
        if (this.items.map { it.label } != items.map { it.label } || this.images != images) {
            bar.setItems(items.mapIndexed { index, item ->
                UITabBarItem(
                    title = item.label,
                    image = images[index],
                    tag = index.toLong()
                ).apply {
                    accessibilityCustomActions = listOf(
                        UIAccessibilityCustomAction(
                            name = Localization.string("action_show_help", item.label),
                            actionHandler = { this@NativeNavigationBarView.onLongPress?.invoke(index); true },
                        )
                    )
                }
            }, animated = false)
        }
        this.items = items
        this.images = images
        this.onLongPress = onLongPress
        this.onHeightChanged = onHeightChanged
        this.isRtl = isRtl
        backgroundColor = if (hasOpaqueBacking) backingColor else UIColor.clearColor
        overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        bar.tintColor = accent
        bar.semanticContentAttribute = if (isRtl) {
            UISemanticContentAttributeForceRightToLeft
        } else {
            UISemanticContentAttributeForceLeftToRight
        }
        bar.unselectedItemTintColor = foreground
        val index = items.indexOfFirst { it.isSelected }
        val selectedItem = bar.items?.getOrNull(index) as? UITabBarItem
        if (bar.selectedItem != selectedItem) bar.selectedItem = selectedItem
    }

    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
        if (isHandlingLongPress) {
            val index = items.indexOfFirst { it.isSelected }
            bar.selectedItem = bar.items?.getOrNull(index) as? UITabBarItem
        } else {
            items.getOrNull(didSelectItem.tag.toInt())?.onClick?.invoke()
        }
    }

    override fun gestureRecognizerShouldBegin(gestureRecognizer: UIGestureRecognizer): Boolean =
        true

    override fun gestureRecognizer(
        gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWithGestureRecognizer: UIGestureRecognizer,
    ): Boolean =
        gestureRecognizer == helpGesture || shouldRecognizeSimultaneouslyWithGestureRecognizer == helpGesture

    /**
     * Resolves a held tab from its equal-width native touch area and opens help once per gesture.
     *
     * @param recognizer Native long press, which cancels the pending tab activation after recognition.
     * @author Dmitriy Moroz
     */
    @ObjCAction
    fun showHelp(recognizer: UILongPressGestureRecognizer) {
        if (recognizer.state == UIGestureRecognizerStateEnded || recognizer.state == UIGestureRecognizerStateCancelled) {
            NSOperationQueue.mainQueue.addOperationWithBlock { isHandlingLongPress = false }
            return
        }
        if (recognizer.state != UIGestureRecognizerStateBegan || items.isEmpty()) return
        isHandlingLongPress = true
        val x = recognizer.locationInView(bar).useContents { x }
        val width = bar.bounds.useContents { size.width }
        if (width <= 0) return
        val visualIndex = floor(x / width * items.size).toInt()
        val index = if (isRtl) items.lastIndex - visualIndex else visualIndex
        if (index in items.indices) onLongPress?.invoke(index)
    }

    /**
     * Releases retained screen callbacks when Compose disposes the native view.
     *
     * @author Dmitriy Moroz
     */
    fun releaseCallbacks() {
        items = emptyList()
        images = emptyList()
        onLongPress = null
        onHeightChanged = null
        bar.delegate = null
        bar.setItems(null, animated = false)
    }
}
