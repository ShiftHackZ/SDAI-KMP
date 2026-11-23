@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.sheet

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Enables system blur for visible iOS sheets outside scaled platform-control illustrations.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun isPlatformSheetBlurAvailable(): Boolean =
    !LocalPlatformControlPreview.current && LocalMviVisibility.current && LocalPlatformControlVisibility.current

/**
 * Samples the underlying application through a native backdrop beneath the shared sheet controls.
 * Compose retains input, state and accessibility instead of creating another content composition.
 *
 * @param color Theme tint matching the enclosing sheet surface.
 * @param modifier Sheet bounds and clipping inherited from its shared surface.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun PlatformSheetBackground(color: Color, modifier: Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    UIKitView(
        modifier = modifier,
        factory = { NativeSheetBackgroundView() },
        update = { it.update(color.toUIColor(), isDark) },
        properties = UIKitInteropProperties(
            interactionMode = null,
            isNativeAccessibilityEnabled = false
        ),
    )
}
