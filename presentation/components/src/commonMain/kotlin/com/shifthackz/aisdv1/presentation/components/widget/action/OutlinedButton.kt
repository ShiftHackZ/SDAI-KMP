package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * Presents a secondary action using the platform surface and shared label/icon content.
 * State stays with the caller; platform rendering preserves the supplied geometry and typography.
 *
 * @param onClick Receives a single activation when the action is enabled.
 * @param modifier Layout and accessibility modifiers for the entire button.
 * @param isEnabled Whether pointer and accessibility activation are available.
 * @param shape Button outline; UIKit uses its corner radius with a native continuous curve.
 * @param contentPadding Shared spacing around the label and icons.
 * @param shouldClipBackground Whether the complete native backing is clipped to the button outline.
 * @param content Compose label, icons or progress content with the shared label text style.
 * @author Dmitriy Moroz
 */
@Composable
expect fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    shouldClipBackground: Boolean = false,
    content: @Composable RowScope.() -> Unit,
)
