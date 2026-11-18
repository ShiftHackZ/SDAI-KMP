package com.shifthackz.aisdv1.presentation.configuration.setup.platform

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.presentation.components.platform.ExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupEffect

@Composable
expect fun rememberServerSetupEffectHandler(
    urlLauncher: ExternalUrlLauncher,
): (ServerSetupEffect) -> Unit
