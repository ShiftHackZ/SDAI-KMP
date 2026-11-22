package com.shifthackz.aisdv1.presentation.appupdate

import androidx.compose.runtime.Composable

/**
 * Delegates native rendering to the implementation compiled for this distribution.
 *
 * @param onFinished Consumes the pending notification.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun NativeAppUpdateHost(onFinished: () -> Unit) {
    DistributionAppUpdateHost(onFinished)
}
