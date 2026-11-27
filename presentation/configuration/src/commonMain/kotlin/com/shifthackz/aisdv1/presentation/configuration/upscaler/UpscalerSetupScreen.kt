package com.shifthackz.aisdv1.presentation.configuration.upscaler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModelStore
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Binds independent upscaler setup to the configuration module's injected ViewModel.
 *
 * @param router Navigation owner retaining the caller beneath this wizard.
 * @param modifier Bounds of the configuration screen.
 * @author Dmitriy Moroz
 */
@Composable
fun UpscalerSetupScreen(router: ServerSetupRouter, modifier: Modifier = Modifier) {
    val koin = remember { getKoin() }
    val viewModel =
        remember(koin, router) { koin.get<UpscalerSetupViewModel> { parametersOf(router) } }
    DisposableEffect(viewModel) {
        val store = ViewModelStore()
        store.put("UpscalerSetup", viewModel)
        onDispose { store.clear() }
    }
    MviComponent(viewModel = viewModel) { state, intent ->
        UpscalerSetupContent(state, intent, modifier)
    }
}
