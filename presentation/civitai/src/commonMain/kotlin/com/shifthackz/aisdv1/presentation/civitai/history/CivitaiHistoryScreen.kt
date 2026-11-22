package com.shifthackz.aisdv1.presentation.civitai.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.router.CivitaiRouter
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Binds the standalone receipt history to its injected viewport owner and native URL handler.
 *
 * @param router Back owner supplied by the application composition root.
 * @param modifier Placement of the complete history route.
 * @author Dmitriy Moroz
 */
@Composable
fun CivitaiHistoryScreen(router: CivitaiRouter, modifier: Modifier = Modifier) {
    val koin = remember { getKoin() }
    val viewModel =
        remember(koin, router) { koin.get<CivitaiHistoryViewModel> { parametersOf(router) } }
    val launcher = rememberExternalUrlLauncher()
    DisposableEffect(viewModel) { onDispose { viewModel.onRouteDisposed() } }
    MviComponent(viewModel) { state, intent ->
        CivitaiHistoryContent(state, intent, launcher::openUrl, modifier)
    }
}
