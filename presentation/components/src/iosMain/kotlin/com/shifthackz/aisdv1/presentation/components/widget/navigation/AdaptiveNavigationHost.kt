@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController

/**
 * Gives modern iOS a navigation container so system tabs can move to the device's control edge.
 * Older systems retain the embedded bar. The supplied Compose controller survives every tab change.
 *
 * @param contentController Application controller whose composition owns routing and screen state.
 * @return The adaptive system container, or the original controller before iOS 27.1.
 * @author Dmitriy Moroz
 */
fun adaptiveNavigationHost(contentController: UIViewController): UIViewController {
    val canAdaptBars = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
        majorVersion > 27 || majorVersion == 27L && minorVersion >= 1
    }
    return if (canAdaptBars) NativeAppNavigationController(contentController) else contentController
}

/**
 * Finds the application container without consulting a global window or screen.
 *
 * @return The containing application navigation host, or null for embedded previews and older iOS.
 * @author Dmitriy Moroz
 */
internal fun UIViewController.appNavigationHost(): NativeAppNavigationController? {
    var controller: UIViewController? = this
    while (controller != null) {
        if (controller is NativeAppNavigationController) return controller
        controller = controller.parentViewController
    }
    return null
}
