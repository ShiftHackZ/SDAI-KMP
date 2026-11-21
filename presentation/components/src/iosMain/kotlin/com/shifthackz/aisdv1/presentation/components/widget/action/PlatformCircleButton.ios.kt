@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import platform.UIKit.UIColor
import com.shifthackz.aisdv1.presentation.components.platform.rememberNativeIconImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility

/**
 * Gives toolbar, generation and gallery actions one native glass circle and a 48-point touch target.
 * Transparent backing avoids rectangular patches and lets UIKit render the same secondary material.
 * Compose owns activation, semantics and enclosing contextual-help gestures.
 * Older systems and tutorial illustrations keep the Material control.
 * Retained hidden routes reserve their layout bounds without keeping a native overlay mounted.
 *
 * @param onClick Enabled short-tap action.
 * @param modifier Placement of the circular control.
 * @param isEnabled Whether the action can activate.
 * @param nativeIcon Optional icon rendered with the complete native control above an opaque app canvas.
 * @param nativeTint Foreground of the native icon, including semantic action colors.
 * @param nativeDescription Accessibility label when native rendering replaces the content slot.
 * @param content Shared icon and its accessibility label.
 * @author Dmitriy Moroz
 */
@Composable
internal fun PlatformCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    nativeIcon: ImageVector? = null,
    nativeTint: Color = MaterialTheme.colorScheme.onSurface,
    nativeDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val hasGlassSupport = remember {
        NSProcessInfo.processInfo.operatingSystemVersion.useContents { majorVersion >= 26 }
    }
    if (!hasGlassSupport || LocalPlatformControlPreview.current) {
        IconButton(onClick, modifier.size(48.dp), enabled = isEnabled, content = content)
        return
    }
    if (!LocalMviVisibility.current || !LocalPlatformControlVisibility.current) {
        Spacer(modifier.size(48.dp))
        return
    }
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    Box(
        modifier.size(48.dp).semantics {
            if (nativeIcon != null && nativeDescription != null) contentDescription =
                nativeDescription
        }.clickable(
            interactionSource = interaction,
            indication = null,
            enabled = isEnabled,
            role = Role.Button,
            onClick = onClick,
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (nativeIcon != null) {
            val image = rememberNativeIconImage(nativeIcon)
            val colors = MaterialTheme.colorScheme
            UIKitView(
                modifier = Modifier.matchParentSize(),
                factory = { NativeGlassButtonView() },
                update = {
                    it.update(
                        isOutlined = true,
                        isEnabled = isEnabled,
                        isPressed = isPressed,
                        isDark = colors.background.luminance() < 0.5f,
                        accent = colors.primary.toUIColor(),
                        backingColor = UIColor.clearColor,
                        cornerRadius = 24.0,
                        image = image,
                        foreground = nativeTint.toUIColor(),
                    )
                },
                properties = UIKitInteropProperties(
                    interactionMode = null,
                    isNativeAccessibilityEnabled = false,
                    placedAsOverlay = true,
                ),
            )
        } else {
            CompositionLocalProvider(LocalPlatformControlBackgroundColor provides Color.Transparent) {
                GlassButtonSurface(
                    isOutlined = true,
                    isEnabled = isEnabled,
                    isPressed = isPressed,
                    shape = CircleShape,
                    modifier = Modifier.matchParentSize().clip(CircleShape),
                )
            }
            content()
        }
    }
}
