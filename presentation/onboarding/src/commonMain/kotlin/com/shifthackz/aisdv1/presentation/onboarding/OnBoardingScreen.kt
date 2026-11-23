package com.shifthackz.aisdv1.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Binds the walkthrough to its launch-specific ViewModel while keeping its renderer graph-independent.
 *
 * @param launchSource First-launch or Settings context controlling walkthrough completion.
 * @param router Navigation owner supplied by the application host.
 * @param modifier Placement of the walkthrough pager and actions.
 * @author Dmitriy Moroz
 */
@Composable
fun OnBoardingScreen(
    launchSource: LaunchSource,
    router: OnBoardingRouter,
    modifier: Modifier = Modifier,
) {
    val koin = remember { getKoin() }
    val viewModel = remember(koin, router, launchSource) {
        koin.get<OnBoardingViewModel> { parametersOf(launchSource, router) }
    }
    MviComponent(viewModel = viewModel) { state, processIntent ->
        OnBoardingContent(
            launchSource = viewModel.launchSource,
            state = state,
            processIntent = processIntent,
            modifier = modifier,
        )
    }
}
