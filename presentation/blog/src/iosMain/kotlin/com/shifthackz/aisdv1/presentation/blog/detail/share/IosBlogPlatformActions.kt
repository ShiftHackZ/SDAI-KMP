package com.shifthackz.aisdv1.presentation.blog.detail.share

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController

/**
 * Presents the native share controller, anchored to the current reader on iPad.
 *
 * @author Dmitriy Moroz
 */
internal class IosBlogPlatformActions : BlogPlatformActions {
    @OptIn(ExperimentalForeignApi::class)
    override fun sharePost(text: String): Boolean {
        val presenter = UIApplication.sharedApplication.windows.filterIsInstance<UIWindow>()
            .firstOrNull(UIWindow::isKeyWindow)?.rootViewController
            ?.topMostPresentedViewController() ?: return false
        if (presenter is UIActivityViewController) return true

        val controller = UIActivityViewController(listOf(text), applicationActivities = null)
        controller.popoverPresentationController?.apply {
            sourceView = presenter.view
            sourceRect = presenter.view.bounds.useContents {
                CGRectMake(size.width - 24.0, 48.0, 1.0, 1.0)
            }
        }
        presenter.presentViewController(controller, animated = true, completion = null)
        return true
    }

    private tailrec fun UIViewController.topMostPresentedViewController(): UIViewController =
        presentedViewController?.topMostPresentedViewController() ?: this
}
