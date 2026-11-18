package com.shifthackz.aisdv1.presentation.app

import com.shifthackz.aisdv1.core.navigation.asHomeTabRoute
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.core.navigation.AppRoute
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import com.shifthackz.aisdv1.presentation.components.widget.image.LocalOriginalImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget
import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.LocalGalleryImageLoader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.extensions.clearFocusOnTap
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageIntent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.generation.img2img.rememberImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageIntent
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageViewModel
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import org.koin.core.parameter.parametersOf

/**
 * Composes the shared application from the graph initialized by its native host.
 * Retains visited home tabs and their generation ViewModels across overlay navigation; inpaint
 * uses the same image-to-image ViewModel. Resuming the host retries sampler loading for both forms.
 *
 * @param modifier Layout and input modifiers applied to the application surface.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun AiSdApp(
    modifier: Modifier = Modifier,
) {
    val koin = remember { getKoin() }
    val preferenceManager = remember(koin) {
        koin.get<PreferenceManager>()
    }
    val router = remember { RootAppRouter() }
    val route by router.route.collectAsState()
    val isDrawerOpen by router.drawerOpen.collectAsState()
    val canNavigateBack by router.canNavigateBack.collectAsState()
    val languageCode by Localization.languageCodeFlow.collectAsState()
    val urlLauncher = rememberExternalUrlLauncher()
    val imageToImagePlatformActions =
        rememberImageToImagePlatformActions(remember(koin) { koin.get<OriginalImageBudget>() })
    val textToImageViewModel = remember(koin, router) {
        koin.get<TextToImageViewModel> {
            parametersOf(router)
        }
    }
    val imageToImageViewModel = remember(koin, router, imageToImagePlatformActions) {
        koin.get<ImageToImageViewModel> {
            parametersOf(router, imageToImagePlatformActions)
        }
    }
    val visitedHomeRoutes = remember { mutableStateListOf<AppRoute>() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        textToImageViewModel.processIntent(TextToImageIntent.RetrySamplers)
        imageToImageViewModel.processIntent(ImageToImageIntent.RetrySamplers)
    }
    var lastHomeRoute by remember { mutableStateOf<AppRoute?>(null) }
    val currentRoute = route
    val selectedHomeRoute = currentRoute.asHomeTabRoute()

    LaunchedEffect(selectedHomeRoute) {
        selectedHomeRoute?.let { homeRoute ->
            lastHomeRoute = homeRoute
            if (!visitedHomeRoutes.contains(homeRoute)) {
                visitedHomeRoutes.add(homeRoute)
            }
        }
    }

    RootBackHandler(
        enabled = isDrawerOpen || canNavigateBack,
        onBack = router::navigateBack,
    )

    CompositionLocalProvider(
        LocalGalleryImageLoader provides remember(koin) { koin.get<GalleryImageLoader>() },
        LocalOriginalImageLoader provides remember(koin) { koin.get<OriginalImageLoader>() }) {
        AiSdAppTheme {
            Surface(
                modifier = modifier
                    .fillMaxSize()
                    .clearFocusOnTap(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val shellHomeRoute = selectedHomeRoute ?: lastHomeRoute
                    val renderedHomeRoutes =
                        (visitedHomeRoutes + listOfNotNull(shellHomeRoute)).distinct()

                    if (shellHomeRoute != null) {
                        HomeRoutesScaffold(
                            languageCode = languageCode,
                            activeRoute = shellHomeRoute,
                            renderedRoutes = renderedHomeRoutes,
                            router = router,
                            buildInfoProvider = koin.get<BuildInfoProvider>(),
                            preferenceManager = preferenceManager,
                            shouldShowHomeChrome = selectedHomeRoute != null,
                            textToImageViewModel = textToImageViewModel,
                            imageToImageViewModel = imageToImageViewModel,
                        )
                    }

                    if (selectedHomeRoute == null) {
                        OverlayRouteContent(
                            languageCode = languageCode,
                            route = currentRoute,
                            koin = koin,
                            router = router,
                            urlLauncher = urlLauncher,
                            imageToImageViewModel = imageToImageViewModel,
                        )
                    }
                }
            }
        }
    }
}
