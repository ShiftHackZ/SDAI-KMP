package com.shifthackz.aisdv1.presentation.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.extensions.clearFocusOnTap
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.presentation.components.extensions.generationBrightnessInteraction
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme

/**
 * Connects the shell's MVI state and Back intent to its platform effects and visual content.
 *
 * @param viewModel Host-owned shell state observer.
 * @param modifier Layout and input modifiers forwarded to the shell surface.
 * @param homeContent Renders retained home tabs with the selected route, visited routes and chrome visibility.
 * @param overlayContent Renders the current standalone destination above the retained home tabs.
 * @author Dmitriy Moroz
 */
@Composable
internal fun AiSdAppShell(
    viewModel: AiSdAppViewModel,
    modifier: Modifier = Modifier,
    homeContent: @Composable (AppRoute, List<AppRoute>, Boolean) -> Unit,
    overlayContent: @Composable (AppRoute) -> Unit,
) {
    MviComponent(viewModel = viewModel) { state, processIntent ->
        RootBackHandler(
            enabled = state.canNavigateBack,
            onBack = { processIntent(AiSdAppIntent.NavigateBack) },
        )
        GenerationScreenProtection(
            isGenerating = state.isForegroundGenerating,
            shouldDimScreen = state.shouldDimScreen,
        ) {
            AiSdAppTheme {
                AiSdAppShellContent(
                    state = state,
                    modifier = modifier,
                    homeContent = homeContent,
                    overlayContent = overlayContent,
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
 * @param modifier Layout and input modifiers applied to the outer surface.
 * @param homeContent Renders retained home tabs with selected route, visited routes and chrome visibility.
 * @param overlayContent Renders a standalone destination above the home scaffold.
 * @author Dmitriy Moroz
 */
@Composable
private fun AiSdAppShellContent(
    state: AiSdAppState,
    modifier: Modifier = Modifier,
    homeContent: @Composable (AppRoute, List<AppRoute>, Boolean) -> Unit,
    overlayContent: @Composable (AppRoute) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize().generationBrightnessInteraction().clearFocusOnTap(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            state.homeRoute?.let { homeRoute ->
                homeContent(homeRoute, state.renderedHomeRoutes, state.shouldShowHomeChrome)
            }
            if (!state.shouldShowHomeChrome) {
                overlayContent(state.route)
            }
        }
    }
}
