@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.surface

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.platform.NativeBlurViewController
import com.shifthackz.aisdv1.presentation.components.platform.rememberThemedNativeContent
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarObscured
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Places panel controls in the same UIKit hierarchy as their clipped system backdrop.
 * Hidden retained routes keep their panel bounds without a native overlay.
 *
 * @param modifier Explicit panel bounds and placement.
 * @param cornerRadius Radius clipping the native backdrop and child composition.
 * @param content Controls in a transparent composition above the material.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformBlurredPanel(
    modifier: Modifier,
    cornerRadius: Dp,
    content: @Composable () -> Unit,
) {
    if (!LocalMviVisibility.current || !LocalPlatformControlVisibility.current || LocalToolbarObscured.current) {
        Spacer(modifier)
        return
    }
    val colors = MaterialTheme.colorScheme
    val nativeContent = rememberThemedNativeContent {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            LocalPlatformControlBackgroundColor provides Color.Transparent,
        ) {
            content()
        }
    }
    UIKitViewController(
        modifier = modifier,
        factory = { NativeBlurViewController(nativeContent) },
        update = {
            it.update(
                true,
                colors.surface.toUIColor(),
                colors.background.luminance() < 0.5f,
                tintAlpha = 0.08,
                cornerRadius = cornerRadius.value.toDouble(),
            )
        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
            placedAsOverlay = true,
        ),
    )
}
