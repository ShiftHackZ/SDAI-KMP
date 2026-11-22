package com.shifthackz.aisdv1.presentation.components.widget.surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Hosts controls above rounded native backdrop material on iOS and a themed surface on Android.
 * The caller supplies the panel's width and height; its content fills those bounds.
 *
 * @param modifier Panel bounds and placement, including any external spacing.
 * @param cornerRadius Radius clipping both the backdrop and its controls.
 * @param content Controls hosted inside the panel.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformBlurredPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit,
)
