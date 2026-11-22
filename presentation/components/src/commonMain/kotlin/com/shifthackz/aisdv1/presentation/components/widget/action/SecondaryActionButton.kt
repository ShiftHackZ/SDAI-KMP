package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Matches primary action dimensions while retaining the outlined treatment for secondary operations.
 *
 * @param onClick Dispatches the selected action.
 * @param modifier Layout modifiers applied before the common button dimensions.
 * @param isEnabled Whether this action is currently available.
 * @param content Visible label and optional decorative icon.
 * @author Dmitriy Moroz
 */
@Composable
fun SecondaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        isEnabled = isEnabled,
        content = content
    )
}
