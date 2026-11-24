package com.shifthackz.aisdv1.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.LifecycleEventEffect
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationPerformanceUseCase
import com.shifthackz.aisdv1.presentation.appupdate.AppUpdateLaunchState
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.LocalGalleryImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.LocalOriginalImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget
import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageLoader
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageIntent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.generation.img2img.rememberImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageIntent
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageViewModel
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerFormViewModel
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Wires the shared application to the graph initialized by its native host.
 * Retains generation ViewModels across tab and overlay navigation; inpaint shares image-to-image.
 * Owns router restoration and composition-bound platform actions, retries samplers on resume,
 * and releases the shell observer when this host leaves composition.
 *
 * @param modifier Layout and input modifiers forwarded to the application surface.
 * @author Dmitriy Moroz
 */
@Composable
fun AiSdApp(
    modifier: Modifier = Modifier,
) {
    val koin = remember { getKoin() }
    val router = rememberSaveable(saver = backupRouterSaver) { RootAppRouter() }
    val urlLauncher = rememberExternalUrlLauncher()
    val imageToImagePlatformActions =
        rememberImageToImagePlatformActions(remember(koin) { koin.get<OriginalImageBudget>() })
    val textToImageViewModel = remember(koin, router) {
        koin.get<TextToImageViewModel> { parametersOf(router) }
    }
    val imageToImageViewModel = remember(koin, router, imageToImagePlatformActions) {
        koin.get<ImageToImageViewModel> { parametersOf(router, imageToImagePlatformActions) }
    }
    val upscalerViewModel = remember(koin, router, imageToImagePlatformActions) {
        koin.get<UpscalerViewModel> { parametersOf(router, imageToImagePlatformActions) }
    }
    val upscalerFormViewModel = remember(koin) { koin.get<UpscalerFormViewModel>() }
    val upscalerFormState by upscalerFormViewModel.state.collectAsState()
    DisposableEffect(upscalerViewModel, upscalerFormViewModel) {
        val store = ViewModelStore()
        store.put("Upscaler", upscalerViewModel)
        store.put("UpscalerForm", upscalerFormViewModel)
        onDispose { store.clear() }
    }
    val generationActivity =
        remember(textToImageViewModel, imageToImageViewModel, upscalerViewModel) {
            combine(
                textToImageViewModel.state.map { it.generating }.distinctUntilChanged(),
                imageToImageViewModel.state.map { it.generating }.distinctUntilChanged(),
                upscalerViewModel.state.map { it.isRunning }.distinctUntilChanged(),
            ) { isTextGenerating, isImageGenerating, isUpscaling -> isTextGenerating || isImageGenerating || isUpscaling }
        }
    val viewModel = remember(koin, router, generationActivity) {
        koin.get<AiSdAppViewModel> { parametersOf(router, generationActivity) }
    }
    DisposableEffect(viewModel) {
        val store = ViewModelStore()
        store.put("AiSdApp", viewModel)
        onDispose { store.clear() }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        textToImageViewModel.processIntent(TextToImageIntent.RetrySamplers)
        imageToImageViewModel.processIntent(ImageToImageIntent.RetrySamplers)
    }
    val preferenceManager = remember(koin) { koin.get<PreferenceManager>() }
    val buildInfoProvider = remember(koin) { koin.get<BuildInfoProvider>() }
    val observePerformance = remember(koin) { koin.get<ObserveGenerationPerformanceUseCase>() }
    CompositionLocalProvider(
        LocalGalleryImageLoader provides remember(koin) { koin.get<GalleryImageLoader>() },
        LocalOriginalImageLoader provides remember(koin) { koin.get<OriginalImageLoader>() },
    ) {
        AiSdAppShell(
            modifier = modifier,
            viewModel = viewModel,
            appUpdateState = remember(koin) { koin.get<AppUpdateLaunchState>() },
            router = router,
            homeContent = { homeRoute, renderedRoutes, shouldShowHomeChrome ->
                HomeRoutesScaffold(
                    activeRoute = homeRoute,
                    renderedRoutes = renderedRoutes,
                    router = router,
                    buildInfoProvider = buildInfoProvider,
                    preferenceManager = preferenceManager,
                    shouldShowHomeChrome = shouldShowHomeChrome,
                    observePerformance = observePerformance,
                    upscalerViewModel = upscalerViewModel,
                    upscalerFormState = upscalerFormState,
                    textToImageViewModel = textToImageViewModel,
                    imageToImageViewModel = imageToImageViewModel,
                )
            },
            overlayContent = { route ->
                OverlayRouteContent(
                    route = route,
                    koin = koin,
                    router = router,
                    urlLauncher = urlLauncher,
                    imageToImageViewModel = imageToImageViewModel,
                )
            },
        )
    }
}
