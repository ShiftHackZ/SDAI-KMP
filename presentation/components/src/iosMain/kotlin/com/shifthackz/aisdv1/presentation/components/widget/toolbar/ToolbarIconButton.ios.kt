package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifthackz.aisdv1.presentation.components.widget.action.PlatformCircleButton

/**
 * Uses the shared native circle renderer for toolbar, generation and gallery actions.
 * Foreground follows the toolbar theme and disabled state; Compose retains semantics and help.
 *
 * @param onClick Enabled action callback.
 * @param modifier Placement of the 48-point target.
 * @param isEnabled Whether short-tap activation is allowed.
 * @param content Shared glyph and accessibility label.
 * @author Dmitriy Moroz
 */
@Composable
actual fun ToolbarIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    isEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    PlatformCircleButton(onClick, modifier, isEnabled) {
        val foreground = MaterialTheme.colorScheme.onSurface
        CompositionLocalProvider(
            LocalContentColor provides foreground.copy(
                alpha = foreground.alpha * if (isEnabled) 1f else 0.38f,
            ),
            content = content,
        )
    }
}

/**
 * Uses the system-style directional chevron for iOS back navigation.
 *
 * @param androidIcon Android-only glyph retained by the common API.
 * @author Dmitriy Moroz
 */
internal actual fun platformToolbarBackIcon(androidIcon: ImageVector): ImageVector =
    Icons.AutoMirrored.Filled.KeyboardArrowLeft
