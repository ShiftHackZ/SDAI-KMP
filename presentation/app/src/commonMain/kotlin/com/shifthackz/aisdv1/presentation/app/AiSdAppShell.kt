package com.shifthackz.aisdv1.presentation.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.extensions.clearFocusOnTap
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.core.navigation.isHomeRoute
import com.shifthackz.aisdv1.presentation.appupdate.AppUpdateHost
import com.shifthackz.aisdv1.presentation.appupdate.AppUpdateLaunchState
import com.shifthackz.aisdv1.presentation.components.extensions.generationBrightnessInteraction
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalBackTransitionState
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformBackHandler
import com.shifthackz.aisdv1.presentation.components.widget.navigation.rememberBackTransitionState

/**
 * Connects the shell's MVI state and Back intent to its platform effects and visual content.
 *
 * @param viewModel Host-owned shell state observer.
 * @param appUpdateState Process-lived update discovery and dismissal state.
 * @param router Navigation owner providing capture timing before a screen is covered.
 * @param modifier Layout and input modifiers forwarded to the shell surface.
 * @param homeContent Renders retained home tabs with the selected route, visited routes and chrome visibility.
 * @param overlayContent Renders the current standalone destination above the retained home tabs.
 * @author Dmitriy Moroz
 */
@Composable
internal fun AiSdAppShell(
    viewModel: AiSdAppViewModel,
    appUpdateState: AppUpdateLaunchState,
    router: RootAppRouter,
    modifier: Modifier = Modifier,
    homeContent: @Composable (AppRoute, List<AppRoute>, Boolean) -> Unit,
    overlayContent: @Composable (AppRoute) -> Unit,
) {
    MviComponent(viewModel = viewModel) { state, processIntent ->
        GenerationScreenProtection(
            isGenerating = state.isForegroundGenerating,
            shouldDimScreen = state.shouldDimScreen,
        ) {
            AiSdAppTheme {
                if (state.shouldShowHomeChrome && !state.isForegroundGenerating) {
                    AppUpdateHost(appUpdateState)
                }
                AiSdAppShellContent(
                    state = state,
                    router = router,
                    modifier = modifier,
                    homeContent = homeContent,
                    overlayContent = overlayContent,
                    onBack = { processIntent(AiSdAppIntent.NavigateBack) },
                )
            }
        }
    }
}

/**
 * Renders the themed application surface and route slots from an explicit shell snapshot.
 * Dependency resolution, state observation and platform effects belong to the host and shell.
 *
 * @param state Shell snapshot determining retained tabs and the foreground destination.
 * @param router Navigation owner providing capture timing before a screen is covered.
 * @param modifier Layout and input modifiers applied to the outer surface.
 * @param homeContent Renders retained home tabs with selected route, visited routes and chrome visibility.
 * @param overlayContent Renders a standalone destination above the home scaffold.
 * @param onBack Commits the router's existing drawer-first Back operation.
 * @author Dmitriy Moroz
 */
@Composable
private fun AiSdAppShellContent(
    state: AiSdAppState,
    router: RootAppRouter,
    modifier: Modifier = Modifier,
    homeContent: @Composable (AppRoute, List<AppRoute>, Boolean) -> Unit,
    overlayContent: @Composable (AppRoute) -> Unit,
    onBack: () -> Unit,
) {
    val backTransition = rememberBackTransitionState()
    Surface(
        modifier = modifier.fillMaxSize().generationBrightnessInteraction().clearFocusOnTap(),
        color = MaterialTheme.colorScheme.background,
    ) {
        CompositionLocalProvider(LocalBackTransitionState provides backTransition) {
            key(state.backStack.lastOrNull()?.id) {
                val hasLocalBackOwner =
                    state.route is AppRoute.Setup || state.route is AppRoute.Backup || state.route == AppRoute.BackupWelcome
                PlatformBackHandler(
                    isEnabled = state.canNavigateBack && !hasLocalBackOwner,
                    canStartGesture = !state.isDrawerOpen && !state.route.isHomeRoute() && state.backStack.size > 1,
                    onBack = onBack,
                )
            }
            NavigationStackHost(
                state,
                backTransition,
                router,
                homeContent = homeContent,
                overlayContent = overlayContent
            )
        }
    }
}
