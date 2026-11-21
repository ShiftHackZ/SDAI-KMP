@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import com.shifthackz.aisdv1.presentation.components.platform.NativeBlurViewController
import com.shifthackz.aisdv1.presentation.components.platform.rememberThemedNativeContent
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import com.shifthackz.aisdv1.presentation.components.platform.rememberNativeIconImage
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Keeps Material drawer gestures and sizing with a native blur sampling Compose and UIKit content.
 * Cooperative touch delivery lets the parent drawer retain its swipe-to-close gesture.
 * @param modifier Sheet placement and measured width.
 * @param content Header and destination rows above the backdrop.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformDrawerSheet(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val nativeContent = rememberThemedNativeContent {
        Column(Modifier.fillMaxSize().padding(safePadding), content = content)
    }
    ModalDrawerSheet(
        modifier,
        drawerContainerColor = Color.Transparent,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        UIKitViewController(
            modifier = Modifier.fillMaxSize(),
            factory = { NativeBlurViewController(nativeContent) },
            update = {
                it.update(
                    isBlurred = true,
                    background = colors.background.toUIColor(),
                    isDark = colors.background.luminance() < 0.5f,
                    tintAlpha = 0.20,
                )
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.Cooperative(),
                isNativeAccessibilityEnabled = true,
                placedAsOverlay = true,
            ),
        )
    }
}

/**
 * Uses a native UIButton's complete glass rendering, including its label and icon, above Compose.
 * The transparent overlay samples the blurred sheet; Compose owns taps, help and semantics.
 * @param label Localized destination name.
 * @param icon Shared vector converted to a native template image.
 * @param isSelected Whether accent glass marks the active route.
 * @param onClick Root navigation callback.
 * @param modifier Destination placement.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformDrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val image = rememberNativeIconImage(icon)
    val colors = MaterialTheme.colorScheme
    Box(
        modifier.fillMaxWidth().height(56.dp).clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = label
                selected = isSelected
            }) {
        UIKitView(
            modifier = Modifier.matchParentSize(),
            factory = { NativeDrawerButtonView() },
            update = {
                it.update(
                    label,
                    image,
                    isSelected,
                    colors.primary.toUIColor(),
                    colors.onSurface.toUIColor(),
                    colors.onPrimary.toUIColor(),
                    colors.background.luminance() < 0.5f
                )
            },
            properties = UIKitInteropProperties(
                interactionMode = null,
                isNativeAccessibilityEnabled = false,
                placedAsOverlay = true
            ),
        )
    }
}
