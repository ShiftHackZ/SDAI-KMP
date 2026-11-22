package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Remembers a capture action for a decorative Back preview, including embedded native controls.
 * Call before replacing the visible screen. Images remain in memory and have no input or lifecycle.
 * Unsupported platforms return null and render their retained Compose layer instead.
 *
 * @author Dmitriy Moroz
 */
@Composable
expect fun rememberNavigationSnapshotCapture(): () -> ImageBitmap?
