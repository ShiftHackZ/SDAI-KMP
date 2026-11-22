package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Uses live retained Compose content for Android transitions.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberNavigationSnapshotCapture(): () -> ImageBitmap? = remember { { null } }
