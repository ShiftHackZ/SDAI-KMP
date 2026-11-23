package com.shifthackz.aisdv1.presentation.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.router.HistoryRouter
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Binds the history renderer to its remembered ViewModel and revision-aware image loader.
 * The screen consumes the initialized application graph while the content renderer stays independent.
 *
 * @param modifier Placement of the history scaffold.
 * @param router Back-navigation owner supplied by the application host.
 * @author Dmitriy Moroz
 */
@Composable
fun HistoryScreen(
    router: HistoryRouter,
    modifier: Modifier = Modifier,
) {
    val koin = remember { getKoin() }
    val viewModel = remember(koin, router) {
        koin.get<HistoryViewModel> { parametersOf(router) }
    }
    val imageLoader = remember(koin) { koin.get<GalleryImageLoader>() }
    MviComponent(viewModel = viewModel) { state, processIntent ->
        HistoryScreenContent(
            state = state,
            processIntent = processIntent,
            modifier = modifier,
            imageLoader = imageLoader,
        )
    }
}
