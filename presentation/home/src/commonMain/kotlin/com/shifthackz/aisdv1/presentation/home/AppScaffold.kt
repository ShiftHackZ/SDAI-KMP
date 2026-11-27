package com.shifthackz.aisdv1.presentation.home

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.core.navigation.isHomeRoute
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityStateProvider

/**
 * Observes settings and connectivity for the home shell and synchronizes its drawer with the router.
 * Destination selections retain the router's existing tab replacement and standalone Back behavior.
 *
 * @param currentRoute Active route controlling drawer selection and home navigation visibility.
 * @param router Shared owner of drawer visibility and destination navigation.
 * @param buildInfoProvider Platform/build metadata shown in branding and provider labels.
 * @param preferenceManager Observed settings controlling provider and developer destinations.
 * @param content Route content receiving a modifier sized inside the scaffold padding.
 * @param modifier Layout modifiers for the outer drawer/scaffold.
 * @param shouldShowHomeChrome Whether the retained shell displays tabs and connectivity chrome.
 * @author Dmitriy Moroz
 */
@Composable
fun AppScaffold(
    currentRoute: AppRoute,
    router: RootAppRouter,
    buildInfoProvider: BuildInfoProvider,
    preferenceManager: PreferenceManager,
    modifier: Modifier = Modifier,
    shouldShowHomeChrome: Boolean = currentRoute.isHomeRoute(),
    content: @Composable (Modifier) -> Unit,
) {
    val isDrawerOpen by router.drawerOpen.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val settings by preferenceManager.observe().collectAsState(Settings())

    LaunchedEffect(isDrawerOpen) {
        when {
            isDrawerOpen && drawerState.isClosed -> drawerState.open()
            !isDrawerOpen && drawerState.isOpen -> drawerState.close()
        }
    }

    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.isOpen }
            .collect { isOpen ->
                if (!isOpen) router.closeDrawer()
            }
    }

    AppScaffoldContent(
        currentRoute = currentRoute,
        settings = settings,
        buildInfoProvider = buildInfoProvider,
        connectivityContent = { content -> ConnectivityStateProvider(content) },
        processAction = { action ->
            when (action) {
                HomeNavigationAction.TextToImage -> router.navigateToTextToImage()
                HomeNavigationAction.ImageToImage -> router.navigateToImageToImage()
                HomeNavigationAction.Upscaler -> router.navigateToUpscaler()
                HomeNavigationAction.Gallery -> router.navigateToGallery()
                HomeNavigationAction.Settings -> router.navigateToSettings()
                HomeNavigationAction.WebUi -> router.navigateToWebUi()
                HomeNavigationAction.Configuration -> router.navigateToServerSetup(LaunchSource.SETTINGS)
                HomeNavigationAction.UpscalerConfiguration -> router.navigateToUpscalerSetup()
                HomeNavigationAction.Debug -> router.navigateToDebugMenu()
            }
        },
        modifier = modifier,
        drawerState = drawerState,
        shouldShowHomeChrome = shouldShowHomeChrome,
        content = content,
    )
}
