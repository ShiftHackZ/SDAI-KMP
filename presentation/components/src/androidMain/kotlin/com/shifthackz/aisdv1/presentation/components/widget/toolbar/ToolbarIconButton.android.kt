package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Preserves the existing Material toolbar control and its default target and ripple.
 *
 * @param onClick Existing action callback.
 * @param modifier Existing placement.
 * @param isEnabled Existing enabled state.
 * @param content Existing icon content and accessible description.
 * @author Dmitriy Moroz
 */
@Composable
actual fun ToolbarIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    isEnabled: Boolean,
    content: @Composable () -> Unit
) {
    IconButton(onClick = onClick, modifier = modifier, enabled = isEnabled, content = content)
}

/**
 * Retains the original filled or outlined Android glyph.
 *
 * @param androidIcon Existing Android vector.
 * @author Dmitriy Moroz
 */
internal actual fun platformToolbarBackIcon(androidIcon: ImageVector): ImageVector = androidIcon
