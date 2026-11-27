package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent

/**
 * Standalone upscaler using shared image acquisition, local progress and final generation dialogs.
 *
 * @param viewModel Root-retained state owner preserving input across configuration and tab changes.
 * @param upscalerFormState Independent catalog snapshot observed by the application root.
 * @param modifier Bounds supplied by the Home route host.
 * @param performanceOverlay Preference-controlled CPU/RAM panel supplied by the composition root.
 * @author Dmitriy Moroz
 */
@Composable
fun UpscalerScreen(
    viewModel: UpscalerViewModel,
    upscalerFormState: UpscalerFormState,
    modifier: Modifier = Modifier,
    performanceOverlay: @Composable () -> Unit = {},
) {
    MviComponent(viewModel = viewModel) { state, intent ->
        UpscalerScreenContent(
            state = state,
            upscalerFormState = upscalerFormState,
            processIntent = intent,
            modifier = modifier,
            performanceOverlay = performanceOverlay,
        )
    }
}
