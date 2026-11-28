package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * Presents a secondary action using the platform surface and shared label/icon content.
 * State stays with the caller. Regular actions share a 48dp minimum height and grow for larger labels;
 * explicit illustration and image-tile bounds remain caller-owned.
 *
 * @param onClick Receives a single activation when the action is enabled.
 * @param modifier Layout and accessibility modifiers for the entire button.
 * @param isEnabled Whether pointer and accessibility activation are available.
 * @param shape Shared outline used to clip the platform surface.
 * @param contentPadding Shared spacing around the label and icons.
 * @param shouldClipBackground Whether the native backing follows the button outline.
 * @param content Compose label, icons or progress content with the shared label text style.
 * @author Dmitriy Moroz
 */
@Composable
actual fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier,
    isEnabled: Boolean,
    shape: Shape,
    contentPadding: PaddingValues,
    shouldClipBackground: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    IosActionButton(
        onClick = onClick,
        modifier = modifier.actionButtonSize(),
        isEnabled = isEnabled,
        shape = shape,
        contentPadding = contentPadding,
        isOutlined = true,
        shouldClipBackground = shouldClipBackground,
        content = content,
    )
}
