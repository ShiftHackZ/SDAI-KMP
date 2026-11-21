@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Uses UIKit switch gestures with shared state and Compose accessibility in scrollable settings rows.
 * The native view stays below the Compose canvas, so drawers, navigation and viewport clips cover it.
 *
 * @param isChecked Caller-owned value synchronized into UIKit on updates.
 * @param onCheckedChange Requested-value callback, or null when the parent row owns activation.
 * @param modifier Layout and semantics for the shared 52dp width and 48dp touch area.
 * @param isEnabled Whether native and accessibility activation are allowed.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformSwitch(
    isChecked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    isEnabled: Boolean,
) {
    if (!LocalMviVisibility.current) {
        Spacer(modifier.size(width = 52.dp, height = 48.dp))
        return
    }
    val accent = MaterialTheme.colorScheme.primary.toUIColor()
    val backingColor = (LocalPlatformControlBackgroundColor.current
        ?: MaterialTheme.colorScheme.background).toUIColor()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    UIKitView(
        modifier = modifier.size(width = 52.dp, height = 48.dp).semantics {
            if (onCheckedChange != null) {
                role = Role.Switch
                toggleableState = ToggleableState(isChecked)
                if (!isEnabled) disabled()
                if (isEnabled) onClick { onCheckedChange(!isChecked); true }
            }
        },
        factory = { NativeSwitchView() },
        update = { view ->
            view.update(
                isChecked,
                isEnabled,
                isDark,
                accent,
                backingColor,
                onCheckedChange
            )
        },
        onRelease = { it.onValueChange = null },
        properties = UIKitInteropProperties(
            interactionMode = if (isEnabled && onCheckedChange != null) UIKitInteropInteractionMode.Cooperative() else null,
            isNativeAccessibilityEnabled = false,
        ),
    )
}
