package com.shifthackz.aisdv1.presentation.gallery.list

import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.core.navigation.router.GalleryRouter
import org.koin.core.parameter.parametersOf

/**
 * Binds the retained gallery ViewModel, sampled image loader and native export effects.
 * System back exits selection only while this retained tab is allowed to handle back.
 *
 * @param modifier Layout modifiers for the gallery route.
 * @param galleryRouter Optional gallery navigation owner; defaults to the initialized application graph.
 * @param backHandlerEnabled Whether this retained tab is visible and may intercept selection-mode back events.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun GalleryScreen(
    modifier: Modifier = Modifier,
    galleryRouter: GalleryRouter? = null,
    backHandlerEnabled: Boolean = true,
) {
    val koin = remember { getKoin() }
    val resolvedGalleryRouter = remember(koin, galleryRouter) {
        galleryRouter ?: koin.get<GalleryRouter>()
    }
    val imageLoader = remember(koin) { koin.get<GalleryImageLoader>() }
    val platformActions = remember(koin) { koin.get<GalleryPlatformActions>() }
    val viewModel = remember(
        koin,
        resolvedGalleryRouter,
    ) {
        koin.get<GalleryViewModel> {
            parametersOf(resolvedGalleryRouter)
        }
    }

    MviComponent(
        viewModel = viewModel,
        processEffect = { effect ->
            when (effect) {
                is GalleryEffect.OpenMediaStoreFolder -> {
                    platformActions.openMediaStoreFolder(effect.folderUri)
                }

                is GalleryEffect.ShareExport -> {
                    platformActions.shareExport(effect.filePath)
                }
            }
        },
    ) { state, intentHandler ->
        GalleryBackHandler(backHandlerEnabled && state.selectionMode) {
            intentHandler(GalleryIntent.ChangeSelectionMode(false))
        }
        GalleryScreenContent(
            modifier = modifier,
            state = state,
            imageLoader = imageLoader,
            processIntent = intentHandler,
        )
    }
}
