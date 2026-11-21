@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)

package com.shifthackz.aisdv1.presentation.components.widget.navigation

import com.shifthackz.aisdv1.presentation.components.platform.rememberNativeIconImage

import androidx.compose.foundation.background
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpAnchor
import kotlinx.coroutines.launch

/**
 * Embeds UIKit's tab bar, retaining shared navigation state and localized long-press help.
 * UIKit renders labels, images, selection and system glass and handles touch interaction.
 * Native accessibility preserves the system's tab hit areas, selection and help actions.
 * Floating native content slides down below the screen while the drawer covers it and back up
 * after dismissal. The reserved height stays fixed and no opaque underlay is introduced.
 *
 * @param items Caller-owned destinations, selected state and short-tap actions.
 * @param modifier Bounds of the bar and its bottom safe area.
 * @param hasBackground Whether the bar has an opaque backing or samples content through system glass.
 * @param isObscured Whether shared chrome covers the bar, concealing floating native content.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformNavigationBar(
    items: List<PlatformNavigationBarItem>,
    modifier: Modifier,
    hasBackground: Boolean,
    isObscured: Boolean,
) {
    if (items.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    val states = items.map { rememberTooltipState(isPersistent = true) }
    val scope = rememberCoroutineScope()
    val images = items.map { rememberNativeIconImage(it.icon) }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var nativeHeight by remember { mutableStateOf(83.dp) }
    val shouldConcealNativeBar = isPlatformNavigationBarFloating && !hasBackground && isObscured
    val verticalOffset by animateDpAsState(
        targetValue = if (shouldConcealNativeBar) nativeHeight else 0.dp,
        animationSpec = tween(durationMillis = 220),
        label = "navigationBarVerticalOffset",
    )
    val shouldOverlayContent = isPlatformNavigationBarFloating && !hasBackground &&
            (isObscured || verticalOffset > 0.dp || states.none { it.isVisible })
    val shouldShowNativeBar = !shouldConcealNativeBar || verticalOffset < nativeHeight
    val canInteractWithNativeBar = !isObscured && verticalOffset == 0.dp
    Box(
        modifier = modifier.fillMaxWidth()
            .background(if (hasBackground) colors.surface else Color.Transparent),
    ) {
        if (!shouldShowNativeBar) {
            Spacer(Modifier.fillMaxWidth().height(nativeHeight))
        } else {
            UIKitView(
                modifier = Modifier.fillMaxWidth().height(nativeHeight).offset(y = verticalOffset),
                factory = { NativeNavigationBarView() },
                update = { view ->
                    view.update(
                        items = items,
                        images = images,
                        isDark = colors.background.luminance() < 0.5f,
                        isRtl = isRtl,
                        accent = colors.primary.toUIColor(),
                        foreground = colors.onSurface.toUIColor(),
                        backingColor = colors.surface.toUIColor(),
                        hasOpaqueBacking = !shouldOverlayContent,
                        onHeightChanged = { height ->
                            if (!shouldConcealNativeBar && verticalOffset == 0.dp) {
                                nativeHeight = height.toFloat().dp
                            }
                        },
                        onLongPress = { index -> scope.launch { states[index].show() }; Unit },
                    )
                },
                onRelease = { it.releaseCallbacks() },
                properties = UIKitInteropProperties(
                    interactionMode = if (canInteractWithNativeBar) UIKitInteropInteractionMode.NonCooperative else null,
                    isNativeAccessibilityEnabled = canInteractWithNativeBar,
                    placedAsOverlay = shouldOverlayContent,
                ),
            )
        }
        Row(Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                Box(Modifier.weight(1f)) {
                    ContextualHelpAnchor(
                        title = item.label,
                        message = item.helpMessage,
                        titleStartIcon = item.icon,
                        state = states[index],
                        shouldHandleGestures = false,
                        shouldExposeAccessibilityAction = false,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Spacer(Modifier.fillMaxWidth().height(nativeHeight))
                    }
                }
            }
        }
    }
}
