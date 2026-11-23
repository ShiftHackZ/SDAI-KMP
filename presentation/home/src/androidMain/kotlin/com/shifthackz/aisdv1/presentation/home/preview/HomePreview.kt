package com.shifthackz.aisdv1.presentation.home.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.home.AppScaffoldContent

/**
 * Composes the real home shell with fixed settings and an initially positioned drawer.
 * Route bodies are empty; the preview covers shell-owned chrome without importing destination modules.
 * No root router, preference observer, connectivity ViewModel or platform service is started.
 *
 * @param scenario Fixed route, drawer and connectivity state to display.
 * @author Dmitriy Moroz
 */
@Composable
internal fun HomePreview(scenario: HomePreviewCase) {
    val drawerState = rememberDrawerState(initialValue = scenario.drawerValue)
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        AppScaffoldContent(
            currentRoute = scenario.route,
            settings = scenario.settings,
            buildInfoProvider = BuildInfoProvider.stub,
            connectivityContent = { content -> content(scenario.connectivity) },
            processAction = {},
            modifier = Modifier.fillMaxSize(),
            drawerState = drawerState,
            shouldShowHomeChrome = scenario.shouldShowHomeChrome,
        ) { routeModifier ->
            HomePreviewRouteContent(route = scenario.route, modifier = routeModifier)
        }
    }
}
