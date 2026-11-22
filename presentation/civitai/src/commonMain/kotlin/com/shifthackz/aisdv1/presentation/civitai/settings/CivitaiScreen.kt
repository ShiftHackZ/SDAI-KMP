package com.shifthackz.aisdv1.presentation.civitai.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.router.CivitaiRouter
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Binds the Civitai account route to the shared application graph.
 *
 * @param router Invoking route's Back owner.
 * @param modifier Placement of the full-screen settings surface.
 * @author Dmitriy Moroz
 */
@Composable
fun CivitaiScreen(router: CivitaiRouter, modifier: Modifier = Modifier) {
    val koin = remember { getKoin() }
    val viewModel = remember(koin, router) { koin.get<CivitaiViewModel> { parametersOf(router) } }
    val launcher = rememberExternalUrlLauncher()
    MviComponent(viewModel) { state, intent ->
        CivitaiContent(state, intent, launcher::openUrl, modifier)
    }
}
