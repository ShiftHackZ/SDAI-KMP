@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

/**
 * Draws a decorative native glass background beneath the shared editor in its existing composition.
 * Keeping input in that composition preserves its focus, selection and immediate editing feedback.
 * Older systems and scaled previews retain the ordinary Material surface.
 *
 * @param modifier Search field bounds and placement.
 * @param content Shared leading icon, hint and editor.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun PlatformSearchFieldContainer(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val hasGlassSupport = remember {
        NSProcessInfo.processInfo.operatingSystemVersion.useContents { majorVersion >= 26 }
    }
    if (!hasGlassSupport || LocalPlatformControlPreview.current) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceVariant
        ) {
            content()
        }
        return
    }
    if (!LocalMviVisibility.current || !LocalPlatformControlVisibility.current) {
        Spacer(modifier)
        return
    }
    val backingColor = (LocalPlatformControlBackgroundColor.current ?: colors.surface).toUIColor()
    Box(modifier) {
        UIKitView(
            modifier = Modifier.matchParentSize(),
            factory = { NativeSearchFieldBackgroundView() },
            update = { it.update(colors.background.luminance() < 0.5f, backingColor) },
            properties = UIKitInteropProperties(
                interactionMode = null,
                isNativeAccessibilityEnabled = false
            ),
        )
        content()
    }
}
