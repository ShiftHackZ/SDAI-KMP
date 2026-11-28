@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSNumber
import platform.Foundation.valueForKey
import platform.Foundation.NSOperationQueue
import platform.CoreGraphics.CGRectContainsPoint
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRectGetMaxY
import platform.CoreGraphics.CGRectGetMaxX
import platform.CoreGraphics.CGAffineTransform
import platform.CoreGraphics.CGAffineTransformMakeTranslation
import platform.UIKit.UIAccessibilityCustomAction
import platform.UIKit.accessibilityCustomActions
import platform.UIKit.accessibilityLabel
import platform.UIKit.accessibilityElementsHidden
import platform.UIKit.UIViewAnimationOptionBeginFromCurrentState
import platform.UIKit.UIViewAnimationOptionCurveEaseInOut
import platform.UIKit.UIViewAnimationOptionAllowUserInteraction
import platform.UIKit.UIGestureRecognizerStateBegan
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UIGestureRecognizerStateCancelled
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UILongPressGestureRecognizer
import platform.UIKit.UIView
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import com.shifthackz.aisdv1.core.localization.Localization
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UITab
import platform.UIKit.UITabBarController
import platform.UIKit.UITabBarControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController

/**
 * Lets UIKit place tabs horizontally or vertically and propagate their safe areas to Compose.
 * Native destination containers sit behind one retained application controller; the shared router remains
 * the source of selection, so a native tap never creates a second screen or navigation stack.
 *
 * @param contentController Full-height composition inside UIKit's horizontal system safe area.
 * Each route reserves any additional native navigation edge independently.
 * @author Dmitriy Moroz
 */
internal class NativeAppNavigationController(
    private val contentController: UIViewController,
) : UIViewController(null, null), UITabBarControllerDelegateProtocol,
    UIGestureRecognizerDelegateProtocol {
    private val tabsController = UITabBarController(null, null)
    private val navigationOverlay = NativeNavigationOverlayView {
        hasInstalledContent && canInteractWithNavigation && owner != null && containsNavigationControl(
            it
        )
    }
    private var canvasColor = UIColor.clearColor
    private var barBackingColor = UIColor.clearColor
    private var hasBarBackground = true
    private var destinations = emptyList<PlatformNavigationBarItem>()
    private var owner: Any? = null
    private var onHelp: ((Int) -> Unit)? = null
    private var hasInstalledContent = false
    private var shouldShowNavigation = false
    private var canInteractWithNavigation = false
    private var isNavigationInputEnabled = true
    private var isNavigationAnimating = false
    private var navigationTransitionId = 0
    private var isHandlingLongPress = false
    private val helpGesture = UILongPressGestureRecognizer(this, NSSelectorFromString("showHelp:"))

    // Kotlin/Native's UIKit bindings predate this public iOS 27.1 trait.
    private val hasVerticalBar: Boolean
        get() = (tabsController.traitCollection.valueForKey("verticalBarEdge") as NSNumber).integerValue != 0L

    private val hasTopBar: Boolean
        get() = !hasVerticalBar &&
                tabsController.contentLayoutGuide.layoutFrame.useContents { origin.y } >
                view.safeAreaInsets.useContents { top }

    /**
     * Bottom protection still needed inside Compose, including a floating bar's scroll-clearance area.
     * The full-height composition reserves this space inside its page body, keeping drawers edge to edge.
     * Side-mounted bars must not leave a bottom-bar-sized gap beside the action area.
     *
     * @author Dmitriy Moroz
     */
    var bottomInset by mutableStateOf(0.dp)
        private set

    /**
     * Top protection shared by route toolbars, including the full safe area below floating iPad tabs.
     * Drawer animations retain this value while the translated native hierarchy changes its safe area.
     *
     * @author Dmitriy Moroz
     */
    var topInset by mutableStateOf(0.dp)
        private set

    /**
     * System home-indicator protection for routes that do not display native tabs.
     *
     * @author Dmitriy Moroz
     */
    var systemBottomInset by mutableStateOf(0.dp)
        private set

    /**
     * Additional left navigation obstruction beyond the system safe area reserved by UIKit.
     *
     * @author Dmitriy Moroz
     */
    var leftInset by mutableStateOf(0.dp)
        private set

    /**
     * Additional right navigation obstruction beyond the system safe area reserved by UIKit.
     *
     * @author Dmitriy Moroz
     */
    var rightInset by mutableStateOf(0.dp)
        private set

    init {
        tabsController.delegate = this
        navigationOverlay.hidden = true
        navigationOverlay.userInteractionEnabled = false
        navigationOverlay.accessibilityElementsHidden = true
        helpGesture.delegate = this
        view.addGestureRecognizer(helpGesture)
        tabsController.setTabs(
            listOf(UITab("", null, "application") {
                UIViewController().apply { view.backgroundColor = UIColor.clearColor }
            }),
            animated = false
        )
        tabsController.setTabBarHidden(true, animated = false)
        addChildViewController(tabsController)
        val tabsView = tabsController.view
        tabsView.translatesAutoresizingMaskIntoConstraints = false
        navigationOverlay.translatesAutoresizingMaskIntoConstraints = false
        navigationOverlay.addSubview(tabsView)
        view.addSubview(navigationOverlay)
        NSLayoutConstraint.activateConstraints(
            listOf(
                navigationOverlay.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
                navigationOverlay.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
                navigationOverlay.topAnchor.constraintEqualToAnchor(view.topAnchor),
                navigationOverlay.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor),
                tabsView.leadingAnchor.constraintEqualToAnchor(navigationOverlay.leadingAnchor),
                tabsView.trailingAnchor.constraintEqualToAnchor(navigationOverlay.trailingAnchor),
                tabsView.topAnchor.constraintEqualToAnchor(navigationOverlay.topAnchor),
                tabsView.bottomAnchor.constraintEqualToAnchor(navigationOverlay.bottomAnchor),
            )
        )
        tabsController.didMoveToParentViewController(this)
        addChildViewController(contentController)
        val contentView = contentController.view
        contentView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(contentView)
        NSLayoutConstraint.activateConstraints(
            listOf(
                contentView.leadingAnchor.constraintEqualToAnchor(view.safeAreaLayoutGuide.leadingAnchor),
                contentView.trailingAnchor.constraintEqualToAnchor(view.safeAreaLayoutGuide.trailingAnchor),
                contentView.topAnchor.constraintEqualToAnchor(view.topAnchor),
                contentView.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor),
            )
        )
        contentController.didMoveToParentViewController(this)
        view.bringSubviewToFront(navigationOverlay)
        hasInstalledContent = true
    }

    /**
     * Updates remaining bottom protection after rotation, folding or native bar placement changes.
     * UIKit can lay out this subclass during its superclass initializer, before Kotlin fields exist.
     *
     * @author Dmitriy Moroz
     */
    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        if (hasInstalledContent) {
            val guideFrame = tabsController.contentLayoutGuide.layoutFrame
            systemBottomInset = view.safeAreaInsets.useContents { bottom.toFloat().dp }
            if (shouldShowNavigation && (!isNavigationAnimating || navigationOverlay.hidden)) {
                topInset = nativeTopInset().toFloat().dp
                leftInset = (guideFrame.useContents { origin.x } -
                        view.safeAreaInsets.useContents { left }).coerceAtLeast(0.0).toFloat().dp
                rightInset = (view.bounds.useContents { size.width } - CGRectGetMaxX(guideFrame) -
                        view.safeAreaInsets.useContents { right }).coerceAtLeast(0.0).toFloat().dp
                val bottomReservation = (view.bounds.useContents { size.height } -
                        CGRectGetMaxY(guideFrame)).coerceAtLeast(0.0)
                bottomInset = if (hasVerticalBar) 0.dp else bottomReservation.toFloat().dp
            }
            updateBarBackground()
        }
    }

    /**
     * Matches native status and control regions to the foreground screen, including standalone routes.
     *
     * @param color Resolved screen canvas, which may differ from the application's default background.
     * @param isDark Whether native system chrome should use its dark appearance.
     * @author Dmitriy Moroz
     */
    fun updateCanvas(color: UIColor, isDark: Boolean) {
        canvasColor = color
        view.backgroundColor = color
        contentController.view.backgroundColor = color
        updateBarBackground()
        overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
    }

    /**
     * Reconciles visible destinations with the shared router and preserves its single composition.
     *
     * @param owner Identity of the foreground bar registration.
     * @param items Current destinations, localized labels, selection and callbacks.
     * @param images Template icons corresponding to the destinations.
     * @param accent Application selection tint.
     * @param foreground Application unselected tint.
     * @param backingColor Active screen's background behind a horizontal navigation bar.
     * @param hasBackground Whether the screen requests an opaque navigation backing.
     * @param isObscured Whether the drawer or another shared surface currently covers navigation.
     * @param isInputEnabled Whether a settled route accepts native taps and help gestures.
     * @param isDark Application appearance, including an override of the system theme.
     * @param onHelp Opens shared contextual help for a destination without selecting it.
     * @author Dmitriy Moroz
     */
    fun update(
        owner: Any,
        items: List<PlatformNavigationBarItem>,
        images: List<UIImage?>,
        accent: UIColor,
        foreground: UIColor,
        backingColor: UIColor,
        hasBackground: Boolean,
        isObscured: Boolean,
        isInputEnabled: Boolean,
        isDark: Boolean,
        onHelp: (Int) -> Unit,
    ) {
        this.owner = owner
        this.onHelp = onHelp
        isNavigationInputEnabled = isInputEnabled
        view.tintColor = accent
        tabsController.view.tintColor = accent
        barBackingColor = backingColor
        hasBarBackground = hasBackground
        updateBarBackground()
        overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        tabsController.tabBar.tintColor = accent
        tabsController.tabBar.unselectedItemTintColor = foreground
        if (destinations.map { it.label } != items.map { it.label }) {
            tabsController.setTabs(items.mapIndexed { index, item ->
                UITab(item.label, images[index], index.toString()) {
                    UIViewController().apply { view.backgroundColor = UIColor.clearColor }
                }.apply {
                    accessibilityCustomActions = listOf(
                        UIAccessibilityCustomAction(
                            name = Localization.string("action_show_help", item.label),
                            actionHandler = {
                                this@NativeAppNavigationController.onHelp?.invoke(
                                    index
                                ); true
                            },
                        )
                    )
                }
            }, animated = false)
        }
        destinations = items
        val index = items.indexOfFirst { it.isSelected }.coerceAtLeast(0)
        tabsController.selectedTab = tabsController.tabs.getOrNull(index) as? UITab
        updateNavigationVisibility(!isObscured)
        if (!isNavigationAnimating) {
            canInteractWithNavigation = shouldShowNavigation && isInputEnabled
            navigationOverlay.userInteractionEnabled = canInteractWithNavigation
            navigationOverlay.accessibilityElementsHidden = !canInteractWithNavigation
        }
        view.setNeedsLayout()
    }

    /**
     * Hides a disposed bar only if another foreground screen has not already replaced it.
     *
     * @param owner Identity supplied by the disposing composition.
     * @author Dmitriy Moroz
     */
    fun clear(owner: Any) {
        if (this.owner !== owner) return
        this.owner = null
        onHelp = null
        updateNavigationVisibility(false)
        updateBarBackground()
        view.setNeedsLayout()
    }

    /**
     * Uses the active screen's footer color beneath UIKit's horizontal glass, while side bars and
     * standalone routes inherit the canvas. Compose paints the status area beneath shared toolbar blur.
     *
     * @author Dmitriy Moroz
     */
    private fun updateBarBackground() {
        tabsController.view.backgroundColor = UIColor.clearColor
        view.backgroundColor =
            if (owner != null && hasBarBackground && !hasVerticalBar) barBackingColor else canvasColor
    }

    /**
     * Slides native navigation off its top, bottom or side edge over 220ms without resizing drawer pages.
     * The hidden native hierarchy retains its measured geometry. Each route independently consumes
     * its navigation insets, so concealment never resizes retained screens or their snapshots.
     * Obsolete animation completions cannot hide a newer bar.
     *
     * @param shouldShow Whether foreground navigation should be visible and interactive after motion.
     * @author Dmitriy Moroz
     */
    private fun updateNavigationVisibility(shouldShow: Boolean) {
        if (shouldShowNavigation == shouldShow) {
            return
        }
        shouldShowNavigation = shouldShow
        isNavigationAnimating = true
        val transitionId = ++navigationTransitionId
        canInteractWithNavigation = false
        navigationOverlay.userInteractionEnabled = false
        navigationOverlay.accessibilityElementsHidden = true
        if (navigationOverlay.hidden) {
            navigationOverlay.transform = CGAffineTransformMakeTranslation(0.0, 0.0)
        }
        tabsController.setTabBarHidden(false, animated = false)
        view.setNeedsLayout()
        view.layoutIfNeeded()
        val hiddenTransform = hiddenNavigationTransform()
        if (navigationOverlay.hidden) navigationOverlay.transform = hiddenTransform
        navigationOverlay.hidden = false
        UIView.animateWithDuration(
            duration = 0.22,
            delay = 0.0,
            options = UIViewAnimationOptionBeginFromCurrentState or
                    UIViewAnimationOptionCurveEaseInOut or UIViewAnimationOptionAllowUserInteraction,
            animations = {
                navigationOverlay.transform =
                    if (shouldShow) CGAffineTransformMakeTranslation(0.0, 0.0) else hiddenTransform
            },
            completion = {
                if (navigationTransitionId == transitionId) {
                    isNavigationAnimating = false
                    canInteractWithNavigation = shouldShow && isNavigationInputEnabled
                    navigationOverlay.userInteractionEnabled = canInteractWithNavigation
                    navigationOverlay.accessibilityElementsHidden = !canInteractWithNavigation
                    navigationOverlay.hidden = !shouldShow
                    if (!shouldShow) {
                        navigationOverlay.transform = CGAffineTransformMakeTranslation(0.0, 0.0)
                    }
                    view.setNeedsLayout()
                }
            },
        )
    }

    /**
     * Moves the full native overlay beyond the current top, bottom or side control region.
     * UIKit's measured guide chooses the physical side, including right-to-left layouts.
     *
     * @return Translation that places navigation outside the application's root bounds.
     * @author Dmitriy Moroz
     */
    private fun hiddenNavigationTransform(): CValue<CGAffineTransform> {
        val guide = tabsController.contentLayoutGuide.layoutFrame
        if (hasTopBar) {
            return CGAffineTransformMakeTranslation(
                0.0,
                -maxOf(topInset.value.toDouble(), nativeTopInset()),
            )
        }
        if (hasVerticalBar) {
            val leading = guide.useContents { origin.x }
            val trailing = (view.bounds.useContents { size.width } - CGRectGetMaxX(guide))
                .coerceAtLeast(0.0)
            return CGAffineTransformMakeTranslation(
                if (leading > trailing) -leading else trailing,
                0.0
            )
        }
        val bar = tabsController.tabBar
        val frame = bar.convertRect(bar.bounds, toView = navigationOverlay)
        val offset =
            (navigationOverlay.bounds.useContents { size.height } - frame.useContents { origin.y })
                .coerceAtLeast(0.0)
        return CGAffineTransformMakeTranslation(0.0, offset)
    }

    /**
     * Includes the full floating-tab safe area, which can extend below the native content guide.
     * Reading the selected native container also preserves the outgoing bar's extent after routing
     * releases Compose's top reservation.
     *
     * @return Top protection in the native container's points.
     * @author Dmitriy Moroz
     */
    private fun nativeTopInset(): Double = maxOf(
        tabsController.contentLayoutGuide.layoutFrame.useContents { origin.y },
        tabsController.selectedViewController?.view?.safeAreaInsets?.useContents { top } ?: 0.0,
    )

    /**
     * Sends native selection through the shared router before reconciling the visible container.
     *
     * @param tabBarController System container requesting a selection.
     * @param shouldSelectTab Destination chosen by a tap or native accessibility action.
     * @return False because caller-owned selection is applied by [update].
     * @author Dmitriy Moroz
     */
    override fun tabBarController(
        tabBarController: UITabBarController,
        shouldSelectTab: UITab,
    ): Boolean {
        val index = tabsController.tabs.indexOf(shouldSelectTab)
        if (!isHandlingLongPress) destinations.getOrNull(index)?.onClick?.invoke()
        return false
    }

    /**
     * Restricts shared help to a native tab outside the application's usable content area.
     *
     * @param gestureRecognizer Hold gesture proposed by UIKit.
     * @return Whether the held native destination has shared help.
     * @author Dmitriy Moroz
     */
    override fun gestureRecognizerShouldBegin(gestureRecognizer: UIGestureRecognizer): Boolean =
        canInteractWithNavigation && owner != null && heldDestinationIndex(gestureRecognizer) >= 0

    /**
     * Keeps contextual help available alongside UIKit's native tab tracking and expansion gesture.
     *
     * @param gestureRecognizer First recognizer competing for the held destination.
     * @param shouldRecognizeSimultaneouslyWithGestureRecognizer UIKit's other active recognizer.
     * @return Whether the shared help recognizer participates in this pair.
     * @author Dmitriy Moroz
     */
    override fun gestureRecognizer(
        gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWithGestureRecognizer: UIGestureRecognizer,
    ): Boolean = gestureRecognizer == helpGesture ||
            shouldRecognizeSimultaneouslyWithGestureRecognizer == helpGesture

    /**
     * Resolves the held system tab by its accessible label, independent of bar axis or RTL order.
     *
     * @param recognizer Native hold gesture inside the system tab bar.
     * @author Dmitriy Moroz
     */
    @ObjCAction
    fun showHelp(recognizer: UILongPressGestureRecognizer) {
        if (recognizer.state == UIGestureRecognizerStateEnded || recognizer.state == UIGestureRecognizerStateCancelled) {
            NSOperationQueue.mainQueue.addOperationWithBlock { isHandlingLongPress = false }
            return
        }
        if (recognizer.state != UIGestureRecognizerStateBegan) return
        val index = heldDestinationIndex(recognizer)
        if (index >= 0) {
            isHandlingLongPress = true
            onHelp?.invoke(index)
        }
    }

    /**
     * Finds the native destination by its accessible label in either bar orientation.
     *
     * @param recognizer Gesture whose location is expressed in the system container.
     * @return Destination index, or -1 for content and unrelated system controls.
     * @author Dmitriy Moroz
     */
    private fun heldDestinationIndex(recognizer: UIGestureRecognizer): Int {
        return destinationIndexAtPoint(recognizer.locationInView(view))
    }

    /**
     * Routes bottom tabs through their public UIKit bounds and top tabs through the native safe area.
     * The legacy tabBar does not describe iPad's floating control region. Side-mounted bars own
     * the area outside the public content guide; their UIKit buttons need not expose view labels.
     *
     * @param point Touch position in navigation-root coordinates.
     * @return Whether visible native navigation owns this touch.
     * @author Dmitriy Moroz
     */
    private fun containsNavigationControl(point: CValue<CGPoint>): Boolean {
        if (hasVerticalBar) {
            val guide = tabsController.contentLayoutGuide.layoutFrame
            return CGRectContainsPoint(view.bounds, point) && point.useContents {
                x < guide.useContents { origin.x } || x > CGRectGetMaxX(guide)
            }
        }
        if (hasTopBar) {
            return CGRectContainsPoint(view.bounds, point) &&
                    point.useContents { y } < topInset.value.toDouble()
        }
        val bar = tabsController.tabBar
        return !bar.hidden && bar.alpha > 0.0 && bar.userInteractionEnabled &&
                CGRectContainsPoint(bar.bounds, bar.convertPoint(point, fromView = view))
    }

    /**
     * Resolves a native destination without relying on private tab-bar presentation classes.
     *
     * @param point Position in the navigation root, shared by its full-size overlay.
     * @return Destination index, or -1 outside visible native controls.
     * @author Dmitriy Moroz
     */
    private fun destinationIndexAtPoint(point: CValue<CGPoint>): Int {
        val pending = mutableListOf<UIView>(navigationOverlay)
        while (pending.isNotEmpty()) {
            val target = pending.removeAt(pending.lastIndex)
            if (target.hidden || target.alpha == 0.0) continue
            val label = target.accessibilityLabel
            val index = destinations.indexOfFirst { it.label == label }
            if (index >= 0 && CGRectContainsPoint(
                    target.bounds,
                    target.convertPoint(point, fromView = view),
                )
            ) return index
            pending.addAll(target.subviews.filterIsInstance<UIView>())
        }
        return -1
    }

}
