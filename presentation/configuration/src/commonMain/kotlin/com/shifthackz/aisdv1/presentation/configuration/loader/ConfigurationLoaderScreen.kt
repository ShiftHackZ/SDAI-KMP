package com.shifthackz.aisdv1.presentation.configuration.loader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.core.navigation.router.ConfigurationLoaderRouter
import org.koin.core.parameter.parametersOf

/**
 * Binds startup configuration loading to a remembered ViewModel from the initialized graph.
 *
 * @param modifier Layout modifiers for the configuration-loading screen.
 * @param router Navigation owner; defaults to the initialized application graph binding.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ConfigurationLoaderScreen(
    modifier: Modifier = Modifier,
    router: ConfigurationLoaderRouter? = null,
) {
    val koin = remember { getKoin() }
    val configurationLoaderRouter =
        remember(koin, router) { router ?: koin.get<ConfigurationLoaderRouter>() }
    val viewModel = remember(koin, configurationLoaderRouter) {
        koin.get<ConfigurationLoaderViewModel> {
            parametersOf(configurationLoaderRouter)
        }
    }
    MviComponent(
        viewModel = viewModel,
    ) { state ->
        ConfigurationLoaderScreenContent(
            modifier = modifier,
            state = state.toContentState(),
        )
    }
}
