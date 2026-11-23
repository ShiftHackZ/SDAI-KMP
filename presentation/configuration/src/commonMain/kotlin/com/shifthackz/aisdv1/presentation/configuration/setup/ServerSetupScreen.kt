package com.shifthackz.aisdv1.presentation.configuration.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.presentation.components.platform.ExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupContent
import com.shifthackz.aisdv1.presentation.configuration.setup.platform.rememberServerSetupEffectHandler
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Binds provider setup to its source-specific ViewModel and native effect handler.
 * Route disposal releases setup-owned work through the existing ViewModel cleanup hook.
 *
 * @param launchSource First-launch or Settings context controlling completion and Back behavior.
 * @param router Navigation owner supplied by the application host.
 * @param urlLauncher Opens external URLs requested by setup effects.
 * @param modifier Placement of the configuration wizard.
 * @author Dmitriy Moroz
 */
@Composable
fun ServerSetupScreen(
    launchSource: LaunchSource,
    router: ServerSetupRouter,
    urlLauncher: ExternalUrlLauncher,
    modifier: Modifier = Modifier,
) {
    val koin = remember { getKoin() }
    val viewModel = remember(koin, router, launchSource) {
        koin.get<ServerSetupViewModel> { parametersOf(launchSource, router) }
    }
    val effectHandler = rememberServerSetupEffectHandler(urlLauncher)
    DisposableEffect(viewModel) { onDispose { viewModel.onRouteDisposed() } }
    MviComponent(viewModel = viewModel, processEffect = effectHandler) { state, processIntent ->
        ServerSetupContent(state = state, processIntent = processIntent, modifier = modifier)
    }
}
