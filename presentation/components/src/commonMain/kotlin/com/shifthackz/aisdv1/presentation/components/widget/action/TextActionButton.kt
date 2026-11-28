package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Presents an unfilled action with the same minimum height as primary and secondary buttons.
 * Used by dialog confirmations, cancellations and inline retry actions across both platforms.
 *
 * @param onClick Dispatches enabled pointer or accessibility activation.
 * @param modifier Placement and outer spacing, before the shared height constraint.
 * @param isEnabled Whether the action may be activated.
 * @param content Localized label and optional decorative icon.
 * @author Dmitriy Moroz
 */
@Composable
fun TextActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.actionButtonSize(),
        enabled = isEnabled,
        content = content,
    )
}
