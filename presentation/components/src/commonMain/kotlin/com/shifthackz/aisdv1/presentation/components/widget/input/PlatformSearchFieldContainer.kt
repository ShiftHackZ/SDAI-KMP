package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Keeps the search editor above a platform-specific rounded background.
 * The caller supplies explicit bounds and retains ownership of input and clear actions.
 *
 * @param modifier Search field bounds and placement.
 * @param content Leading icon, hint and editor filling those bounds.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun PlatformSearchFieldContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
