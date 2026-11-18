package com.shifthackz.aisdv1.presentation.configuration.setup.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.shifthackz.aisdv1.presentation.components.platform.ExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupEffect

@Composable
actual fun rememberServerSetupEffectHandler(
    urlLauncher: ExternalUrlLauncher,
): (ServerSetupEffect) -> Unit {
    val keyboardController = LocalSoftwareKeyboardController.current
    return remember(keyboardController, urlLauncher) {
        { effect ->
            when (effect) {
                ServerSetupEffect.HideKeyboard -> keyboardController?.hide()
                ServerSetupEffect.LaunchManageStoragePermission -> Unit
                is ServerSetupEffect.OpenUrl -> urlLauncher.openUrl(effect.url)
            }
        }
    }
}
