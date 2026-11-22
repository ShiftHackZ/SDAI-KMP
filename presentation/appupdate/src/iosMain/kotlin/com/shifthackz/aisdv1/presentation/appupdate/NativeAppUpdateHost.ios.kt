package com.shifthackz.aisdv1.presentation.appupdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * iOS installation is handled by the App Store destination of the shared prompt.
 *
 * @param onFinished Consumes an unsupported native notification.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun NativeAppUpdateHost(onFinished: () -> Unit) {
    LaunchedEffect(Unit) { onFinished() }
}
