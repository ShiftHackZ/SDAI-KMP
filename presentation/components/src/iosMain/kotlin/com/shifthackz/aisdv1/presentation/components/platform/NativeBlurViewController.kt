@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)

package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle.UIBlurEffectStyleSystemUltraThinMaterial
import platform.UIKit.UIColor
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIVisualEffectView
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController

/**
 * Places a system blur above the app canvas and UIKit inputs, with transparent Compose content above it.
 * The native backdrop samples the final view hierarchy, including prompt editors that cannot be
 * captured by a Compose-only graphics layer. Shared slots retain their state, actions and semantics.
 *
 * @param content Controls rendered in a transparent child composition.
 * @author Dmitriy Moroz
 */
internal class NativeBlurViewController(content: @Composable () -> Unit) :
    UIViewController(null, null) {
    private val backdrop = UIVisualEffectView(effect = null)
    private val tint = UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0))
    private val chrome = ComposeUIViewController(configure = { opaque = false }, content = content)
    private var wasBlurred: Boolean? = null

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.clearColor
        backdrop.userInteractionEnabled = false
        tint.userInteractionEnabled = false
        view.addSubview(backdrop)
        view.addSubview(tint)
        addChildViewController(chrome)
        view.addSubview(chrome.view)
        chrome.didMoveToParentViewController(this)
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        backdrop.setFrame(view.bounds)
        tint.setFrame(view.bounds)
        chrome.view.setFrame(view.bounds)
    }

    /**
     * Shows a matching opaque screen color or a lightly tinted system material.
     * Tint remains a separate overlay so the effect view itself stays fully opaque as UIKit requires.
     *
     * @param isBlurred Whether underlying content remains visible through system material.
     * @param background App background color for the resting surface and translucent tint.
     * @param isDark App appearance, including manual theme overrides.
     * @param tintAlpha Additional background tint above the native material.
     * @author Dmitriy Moroz
     */
    fun update(isBlurred: Boolean, background: UIColor, isDark: Boolean, tintAlpha: Double) {
        view.overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        if (wasBlurred != isBlurred) {
            backdrop.effect =
                if (isBlurred) UIBlurEffect.effectWithStyle(UIBlurEffectStyleSystemUltraThinMaterial) else null
            wasBlurred = isBlurred
        }
        tint.backgroundColor = background.colorWithAlphaComponent(if (isBlurred) tintAlpha else 1.0)
    }
}
