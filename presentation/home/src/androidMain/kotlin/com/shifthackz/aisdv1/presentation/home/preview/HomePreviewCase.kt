package com.shifthackz.aisdv1.presentation.home.preview

import androidx.compose.material3.DrawerValue
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.isHomeRoute
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityState

/**
 * Describes one home-shell snapshot without application observers or navigation history.
 *
 * @param name Label identifying the shell configuration in the preview list.
 * @param route Active route determining tab and drawer selection.
 * @param settings Provider capabilities and developer-menu visibility.
 * @param connectivity Connection result and monitoring visibility.
 * @param drawerValue Initial drawer position, set before the first frame.
 * @param shouldShowHomeChrome Whether the retained shell exposes its persistent navigation.
 * @author Dmitriy Moroz
 */
internal data class HomePreviewCase(
    val name: String,
    val route: AppRoute = AppRoute.TextToImage,
    val settings: Settings = Settings(),
    val connectivity: ConnectivityState = ConnectivityState.Connected(),
    val drawerValue: DrawerValue = DrawerValue.Closed,
    val shouldShowHomeChrome: Boolean = route.isHomeRoute(),
)
