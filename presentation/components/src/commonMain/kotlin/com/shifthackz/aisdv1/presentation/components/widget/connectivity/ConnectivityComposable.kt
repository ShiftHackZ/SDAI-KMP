package com.shifthackz.aisdv1.presentation.components.widget.connectivity

import androidx.compose.ui.Modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin

/**
 * Observes provider connectivity through a remembered ViewModel from the initialized graph.
 *
 * @param modifier Layout modifiers for the status indicator.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ConnectivityComposable(
    modifier: Modifier = Modifier,
) {
    val koin = remember { getKoin() }
    val viewModel = remember(koin) {
        koin.get<ConnectivityViewModel>()
    }
    MviComponent(
        viewModel = viewModel,
    ) { state ->
        ConnectivityWidget(
            state = state,
            modifier = modifier,
        )
    }
}
