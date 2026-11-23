package com.shifthackz.aisdv1.presentation.home.preview

import androidx.compose.material3.DrawerValue
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityState

/**
 * Supplies every tab and drawer selection, connection outcome and provider-dependent drawer shape.
 * Hidden retained chrome and standalone routes demonstrate the absence of home navigation.
 *
 * @author Dmitriy Moroz
 */
internal class HomePreviewParameterProvider : PreviewParameterProvider<HomePreviewCase> {
    private val cases = listOf(
        HomePreviewCase("Text to image"),
        HomePreviewCase("Image to image", route = AppRoute.ImageToImage),
        HomePreviewCase("Gallery", route = AppRoute.Gallery),
        HomePreviewCase("Settings", route = AppRoute.Settings),
        HomePreviewCase("Legacy home alias", route = AppRoute.Home),
        HomePreviewCase("Checking connection", connectivity = ConnectivityState.Uninitialized()),
        HomePreviewCase("Disconnected", connectivity = ConnectivityState.Disconnected()),
        HomePreviewCase(
            "Monitoring hidden",
            connectivity = ConnectivityState.Connected(enabled = false)
        ),
        HomePreviewCase(
            "Retained chrome hidden",
            route = AppRoute.Settings,
            shouldShowHomeChrome = false
        ),
        HomePreviewCase("Standalone route", route = AppRoute.Logger),
        HomePreviewCase("Drawer · Text to image", drawerValue = DrawerValue.Open),
        HomePreviewCase(
            "Drawer · Image to image",
            route = AppRoute.ImageToImage,
            drawerValue = DrawerValue.Open
        ),
        HomePreviewCase(
            "Drawer · Gallery",
            route = AppRoute.Gallery,
            drawerValue = DrawerValue.Open
        ),
        HomePreviewCase(
            "Drawer · Settings",
            route = AppRoute.Settings,
            drawerValue = DrawerValue.Open
        ),
        HomePreviewCase("Drawer · Web UI", route = AppRoute.WebUi, drawerValue = DrawerValue.Open),
        HomePreviewCase(
            "Drawer · Configuration", route = AppRoute.Setup(LaunchSource.SETTINGS),
            drawerValue = DrawerValue.Open,
        ),
        HomePreviewCase(
            "Drawer · Developer menu", route = AppRoute.Debug,
            settings = Settings(developerMode = true), drawerValue = DrawerValue.Open,
        ),
        HomePreviewCase(
            "Drawer · Gallery detail", route = AppRoute.GalleryDetail(itemId = 1),
            drawerValue = DrawerValue.Open,
        ),
        HomePreviewCase(
            "Drawer · Local provider with developer mode",
            settings = Settings(source = ServerSource.LOCAL_MNN, developerMode = true),
            drawerValue = DrawerValue.Open,
        ),
        HomePreviewCase(
            "Drawer · Cloud provider", settings = Settings(source = ServerSource.STABILITY_AI),
            drawerValue = DrawerValue.Open,
        ),
        HomePreviewCase(
            "Drawer · Long provider label",
            route = AppRoute.WebUi,
            settings = Settings(source = ServerSource.OPEN_AI_COMPATIBLE),
            drawerValue = DrawerValue.Open,
        ),
    )

    /**
     * Fixed shell states rendered by the light and dark preview annotations.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<HomePreviewCase> = cases.asSequence()

    /**
     * Identifies the route, drawer capability or connectivity state shown in each preview.
     *
     * @param index Position in the ordered scenario list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
