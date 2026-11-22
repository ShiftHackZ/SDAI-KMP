package com.shifthackz.aisdv1.presentation.appupdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher

/**
 * Owns the optional launch prompt within the application's themed composition.
 *
 * @param state Injected process-lived discovery and dismissal state.
 * @author Dmitriy Moroz
 */
@Composable
fun AppUpdateHost(state: AppUpdateLaunchState) {
    val update by state.update.collectAsState()
    val launcher = rememberExternalUrlLauncher()
    LaunchedEffect(state) { state.checkOnce() }
    update?.let { available ->
        val url = available.url
        if (url == null) {
            NativeAppUpdateHost(onFinished = state::dismiss)
        } else {
            AppUpdateDialog(
                version = available.version,
                onUpdate = {
                    launcher.openUrl(url)
                    state.dismiss()
                },
                onDismiss = state::dismiss,
            )
        }
    }
}

/**
 * Presents the native updater only for an eligible Play installation.
 *
 * @param onFinished Consumes this launch's notification on cancellation or failure.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun NativeAppUpdateHost(onFinished: () -> Unit)
