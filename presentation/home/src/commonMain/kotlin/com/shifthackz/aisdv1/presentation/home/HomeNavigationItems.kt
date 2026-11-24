package com.shifthackz.aisdv1.presentation.home

import com.shifthackz.aisdv1.presentation.components.model.generationConfigurationIcon
import com.shifthackz.aisdv1.presentation.components.model.upscalerConfigurationIcon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Web
import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.presentation.components.widget.source.getName

/**
 * Builds the five persistent home tabs using localized labels and caller-owned selection.
 *
 * @param currentRoute Active destination, including the legacy home alias.
 * @param onAction Receives the selected home destination.
 * @author Dmitriy Moroz
 */
@Composable
internal fun appBottomNavigationItems(
    currentRoute: AppRoute,
    onAction: (HomeNavigationAction) -> Unit,
): List<HomeNavigationBarItem> = listOf(
    HomeNavigationBarItem(
        label = Localization.string("home_tab_txt_to_img"),
        selected = currentRoute == AppRoute.Home || currentRoute == AppRoute.TextToImage,
        icon = Icons.Default.TextFields,
        helpMessage = Localization.string("home_help_txt_to_img"),
        onClick = { onAction(HomeNavigationAction.TextToImage) },
    ),
    HomeNavigationBarItem(
        label = Localization.string("home_tab_img_to_img"),
        selected = currentRoute == AppRoute.ImageToImage,
        icon = Icons.Default.Image,
        helpMessage = Localization.string("home_help_img_to_img"),
        onClick = { onAction(HomeNavigationAction.ImageToImage) },
    ),
    HomeNavigationBarItem(
        label = Localization.string("upscaler_title"),
        selected = currentRoute == AppRoute.Upscaler,
        icon = Icons.Default.PhotoSizeSelectLarge,
        helpMessage = Localization.string("upscaler_provider_help"),
        onClick = { onAction(HomeNavigationAction.Upscaler) },
    ),
    HomeNavigationBarItem(
        label = Localization.string("home_tab_gallery"),
        selected = currentRoute == AppRoute.Gallery,
        icon = Icons.Default.PhotoLibrary,
        helpMessage = Localization.string("home_help_gallery"),
        onClick = { onAction(HomeNavigationAction.Gallery) },
    ),
    HomeNavigationBarItem(
        label = Localization.string("home_tab_settings"),
        selected = currentRoute == AppRoute.Settings,
        icon = Icons.Default.Settings,
        helpMessage = Localization.string("home_help_settings"),
        onClick = { onAction(HomeNavigationAction.Settings) },
    ),
)

/**
 * Builds drawer destinations, including the selected provider's web UI and optional developer menu.
 *
 * @param currentRoute Active route used for selection, including gallery details and configuration.
 * @param settings Provider capabilities and developer-menu visibility.
 * @param buildInfoProvider Platform used for provider display names.
 * @param onAction Receives the selected drawer destination.
 * @author Dmitriy Moroz
 */
@Composable
internal fun appDrawerItems(
    currentRoute: AppRoute,
    settings: Settings,
    buildInfoProvider: BuildInfoProvider,
    onAction: (HomeNavigationAction) -> Unit,
): List<DrawerSheetItem> {
    val sourceName = settings.source.getName(buildInfoProvider.platform)
    return buildList {
        add(
            DrawerSheetItem(
                label = Localization.string("title_text_to_image"),
                selected = currentRoute == AppRoute.Home || currentRoute == AppRoute.TextToImage,
                icon = Icons.Default.TextFields,
                helpMessage = Localization.string("home_help_txt_to_img"),
                onClick = { onAction(HomeNavigationAction.TextToImage) },
            ),
        )
        add(
            DrawerSheetItem(
                label = Localization.string("title_image_to_image"),
                selected = currentRoute == AppRoute.ImageToImage,
                icon = Icons.Default.Image,
                helpMessage = Localization.string("home_help_img_to_img"),
                onClick = { onAction(HomeNavigationAction.ImageToImage) },
            ),
        )
        add(
            DrawerSheetItem(
                label = Localization.string("upscaler_title"),
                selected = currentRoute == AppRoute.Upscaler,
                icon = Icons.Default.PhotoSizeSelectLarge,
                helpMessage = Localization.string("upscaler_provider_help"),
                onClick = { onAction(HomeNavigationAction.Upscaler) })
        )
        add(
            DrawerSheetItem(
                label = Localization.string("title_gallery"),
                selected = currentRoute == AppRoute.Gallery || currentRoute is AppRoute.GalleryDetail,
                icon = Icons.Default.PhotoLibrary,
                helpMessage = Localization.string("home_help_gallery"),
                onClick = { onAction(HomeNavigationAction.Gallery) },
            ),
        )
        if (settings.source.featureTags.contains(FeatureTag.OwnServer)) {
            add(
                DrawerSheetItem(
                    label = "${Localization.string("drawer_web_ui")} ($sourceName)",
                    selected = currentRoute == AppRoute.WebUi,
                    icon = Icons.Default.Web,
                    helpMessage = Localization.string("home_help_web_ui"),
                    onClick = { onAction(HomeNavigationAction.WebUi) },
                ),
            )
        }
        add(
            DrawerSheetItem(
                label = Localization.string("title_settings"),
                selected = currentRoute == AppRoute.Settings,
                icon = Icons.Default.Settings,
                helpMessage = Localization.string("home_help_settings"),
                onClick = { onAction(HomeNavigationAction.Settings) },
            ),
        )
        add(
            DrawerSheetItem(
                label = Localization.string("settings_item_config"),
                selected = currentRoute is AppRoute.Setup && !currentRoute.isUpscaler,
                icon = generationConfigurationIcon,
                helpMessage = Localization.string("settings_help_configuration"),
                onClick = { onAction(HomeNavigationAction.Configuration) },
            ),
        )
        add(
            DrawerSheetItem(
                label = Localization.string("upscaler_configuration"),
                selected = currentRoute is AppRoute.Setup && currentRoute.isUpscaler,
                icon = upscalerConfigurationIcon,
                helpMessage = Localization.string("upscaler_provider_help"),
                onClick = { onAction(HomeNavigationAction.UpscalerConfiguration) },
            ),
        )
        if (settings.developerMode) {
            add(
                DrawerSheetItem(
                    label = Localization.string("title_debug_menu"),
                    selected = currentRoute == AppRoute.Debug,
                    icon = Icons.Default.DeveloperMode,
                    helpMessage = Localization.string("settings_help_debug_menu"),
                    onClick = { onAction(HomeNavigationAction.Debug) },
                ),
            )
        }
    }
}
