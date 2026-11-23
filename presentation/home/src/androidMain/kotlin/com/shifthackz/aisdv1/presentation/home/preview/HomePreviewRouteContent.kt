package com.shifthackz.aisdv1.presentation.home.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton

/**
 * Supplies a route toolbar and empty body so shell previews retain realistic content boundaries.
 * Destination-specific forms, galleries and settings have their own module previews.
 *
 * @param route Route whose localized heading is shown.
 * @param modifier Available route area supplied by the home shell.
 * @author Dmitriy Moroz
 */
@Composable
internal fun HomePreviewRouteContent(route: AppRoute, modifier: Modifier = Modifier) {
    val titleKey = when (route) {
        AppRoute.ImageToImage -> "title_image_to_image"
        AppRoute.Gallery, is AppRoute.GalleryDetail -> "title_gallery"
        AppRoute.Settings -> "title_settings"
        AppRoute.WebUi -> "drawer_web_ui"
        is AppRoute.Setup -> "settings_item_config"
        AppRoute.Debug -> "title_debug_menu"
        AppRoute.Logger -> "title_debug_logger"
        else -> "title_text_to_image"
    }
    PlatformScaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PlatformTopAppBar(
                title = { Text(Localization.string(titleKey)) },
                navigationIcon = {
                    ToolbarIconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        },
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues))
    }
}
