package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable

/**
 * Observes physical iOS rotation, including control-edge changes that keep the window size unchanged.
 * Android retains its existing activity and drawer behavior without invoking the callback.
 *
 * @param onRotate Called on the UI thread when the device changes to another valid orientation.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformRotationEffect(onRotate: () -> Unit)
