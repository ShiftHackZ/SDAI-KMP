@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Keeps native child controls inside their animated viewport, including their glass backgrounds.
 *
 * @param modifier Measured bounds of the clipping viewport.
 * @param content Control group retaining its current app appearance.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformClippedContent(modifier: Modifier, content: @Composable () -> Unit) {
    val background = LocalPlatformControlBackgroundColor.current
    val nativeContent = rememberThemedNativeContent {
        CompositionLocalProvider(
            LocalPlatformControlBackgroundColor provides background,
            content = content
        )
    }
    UIKitViewController(
        modifier = modifier,
        factory = { NativeClippedContentController(nativeContent) },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
            placedAsOverlay = true,
        ),
    )
}
