@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)

package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.ui.uikit.OnFocusBehavior
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController

/**
 * Gives a small native control group a UIKit clipping ancestor without creating another screen.
 * Its transparent child retains native density, input and accessibility owners.
 *
 * @param content Control group occupying the clipped viewport.
 * @author Dmitriy Moroz
 */
internal class NativeClippedContentController(content: @Composable () -> Unit) :
    UIViewController(null, null) {
    private val controls = ComposeUIViewController(
        configure = {
            opaque = false
            onFocusBehavior = OnFocusBehavior.DoNothing
        },
        content = content,
    )

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.clearColor
        view.clipsToBounds = true
        addChildViewController(controls)
        view.addSubview(controls.view)
        controls.didMoveToParentViewController(this)
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        controls.view.setFrame(view.bounds)
    }
}
