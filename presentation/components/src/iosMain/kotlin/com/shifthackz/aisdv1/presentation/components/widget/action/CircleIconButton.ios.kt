@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview

/**
 * Places a shared icon over a native glass UIButton surface on iOS 26 and newer.
 * Compose owns activation and semantics, preserving enclosing tooltip long-press handling.
 * Older iOS keeps the existing Material fallback without calling unavailable glass selectors.
 *
 * @param onClick Receives enabled activation.
 * @param icon Shared action vector.
 * @param contentDescription Localized action announced by accessibility.
 * @param modifier Placement of the 48dp circular button.
 * @param isEnabled Whether activation is allowed.
 * @param tint Shared foreground color for selected, normal or destructive actions.
 * @author Dmitriy Moroz
 */
@Composable
actual fun CircleIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier,
    isEnabled: Boolean,
    tint: Color,
) {
    val hasGlassSupport = remember {
        NSProcessInfo.processInfo.operatingSystemVersion.useContents { majorVersion >= 26 }
    }
    if (!hasGlassSupport || LocalPlatformControlPreview.current) {
        IconButton(onClick = onClick, modifier = modifier.size(48.dp), enabled = isEnabled) {
            Icon(
                icon,
                contentDescription,
                Modifier.size(24.dp),
                if (isEnabled) tint else tint.copy(alpha = 0.38f)
            )
        }
        return
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = modifier.size(48.dp).clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = isEnabled,
            role = Role.Button,
            onClick = onClick,
        ),
        contentAlignment = Alignment.Center,
    ) {
        GlassButtonSurface(
            isOutlined = true,
            isEnabled = isEnabled,
            isPressed = isPressed,
            shape = CircleShape,
            modifier = Modifier.matchParentSize(),
        )
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = if (isEnabled) tint else tint.copy(alpha = 0.38f),
        )
    }
}
