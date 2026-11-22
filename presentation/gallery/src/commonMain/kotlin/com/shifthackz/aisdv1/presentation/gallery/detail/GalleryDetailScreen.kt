@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.gallery.detail

import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.core.navigation.router.GalleryDetailRouter
import org.koin.core.parameter.parametersOf

/**
 * Binds a remembered detail ViewModel and shared sampled loader for a selected generation identity.
 *
 * @param itemId Initial persistent identity, or nonpositive to open the last cached result.
 * @param modifier Layout modifiers for the detail screen.
 * @param router Optional detail route owner; defaults to the initialized application graph.
 * @param civitaiAction App-supplied Civitai publishing action, retaining independent screen-module ownership.
 * @author Dmitriy Moroz
 */
@Composable
fun GalleryDetailScreen(
    itemId: Long,
    modifier: Modifier = Modifier,
    router: GalleryDetailRouter? = null,
    civitaiAction: @Composable (Long) -> Unit = {},
) {
    val koin = remember { getKoin() }
    val resolvedRouter = remember(koin, router) {
        router ?: koin.get<GalleryDetailRouter>()
    }
    val imageLoader = remember(koin) { koin.get<GalleryImageLoader>() }
    val platformActions = remember(koin) { koin.get<GalleryDetailPlatformActions>() }
    val viewModel = remember(
        koin,
        itemId,
        resolvedRouter,
        platformActions,
    ) {
        koin.get<GalleryDetailViewModel> {
            parametersOf(itemId, resolvedRouter, platformActions)
        }
    }

    MviComponent(viewModel = viewModel) { state, intentHandler ->
        GalleryDetailScreenContent(
            modifier = modifier.fillMaxSize(),
            state = state,
            imageLoader = imageLoader,
            civitaiAction = { state.content?.id?.let { civitaiAction(it) } },
            processIntent = intentHandler,
        )
    }
}
