package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable

/**
 * Leaves Android rotation and drawer handling with the existing activity configuration.
 *
 * @param onRotate Shared iOS callback, intentionally unused on Android.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformRotationEffect(onRotate: () -> Unit) = Unit
