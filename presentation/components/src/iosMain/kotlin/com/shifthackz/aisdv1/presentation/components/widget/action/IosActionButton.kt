@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton as MaterialOutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

/**
 * Keeps Compose content, measurement, activation and accessibility above a decorative UIKit surface.
 * Older iOS releases use the existing Material renderer without calling unavailable glass APIs.
 *
 * @param onClick Receives enabled button activation, including accessibility activation.
 * @param modifier Layout and semantics for the complete button.
 * @param isEnabled Whether this action may be activated.
 * @param shape Shared button outline for native surface clipping.
 * @param contentPadding Shared label/icon spacing.
 * @param isOutlined Whether to use the secondary glass appearance and shared outline.
 * @param content Shared label, icon or progress composition.
 * @author Dmitriy Moroz
 */
@Composable
internal fun IosActionButton(
    onClick: () -> Unit,
    modifier: Modifier,
    isEnabled: Boolean,
    shape: Shape,
    contentPadding: PaddingValues,
    isOutlined: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    val hasGlassSupport = remember {
        NSProcessInfo.processInfo.operatingSystemVersion.useContents { majorVersion >= 26 }
    }
    if (!hasGlassSupport) {
        if (isOutlined) {
            MaterialOutlinedButton(
                onClick,
                modifier,
                isEnabled,
                shape,
                contentPadding = contentPadding,
                content = content
            )
        } else {
            Button(
                onClick,
                modifier,
                isEnabled,
                shape,
                contentPadding = contentPadding,
                content = content
            )
        }
        return
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val colors =
        if (isOutlined) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors()
    val contentColor = if (isEnabled) colors.contentColor else colors.disabledContentColor
    val border = if (isOutlined) ButtonDefaults.outlinedButtonBorder(isEnabled) else null
    Box(
        modifier = modifier.minimumInteractiveComponentSize().clip(shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        GlassButtonSurface(
            modifier = Modifier.matchParentSize(),
            isOutlined = isOutlined,
            isEnabled = isEnabled,
            isPressed = isPressed,
            shape = shape,
        )
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                Row(
                    modifier = Modifier.defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight,
                    ).padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content,
                )
            }
        }
    }
}
