package com.shifthackz.aisdv1.presentation.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.core.navigation.router.SplashRouter
import org.koin.core.parameter.parametersOf

/**
 * Shows startup progress while the remembered splash ViewModel selects the initial route.
 * The native host must initialize the application graph before composing this screen.
 *
 * @param modifier Layout modifiers for the startup-progress surface.
 * @param router Navigation owner; defaults to the initialized application graph binding.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    router: SplashRouter? = null,
) {
    val koin = remember { getKoin() }
    val splashRouter = remember(koin, router) { router ?: koin.get<SplashRouter>() }
    val viewModel = remember(koin, splashRouter) {
        koin.get<SplashViewModel> {
            parametersOf(splashRouter)
        }
    }
    MviComponent(
        viewModel = viewModel,
    ) {
        SplashScreenContent(
            modifier = modifier,
        )
    }
}
