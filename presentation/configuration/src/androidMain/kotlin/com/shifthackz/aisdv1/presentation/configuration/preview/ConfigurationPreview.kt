package com.shifthackz.aisdv1.presentation.configuration.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupContent
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.source.visibleSources

/**
 * Applies the application palette without system-bar effects to configuration preview content.
 *
 * @param content Stateless wizard, loader, sheet or download-source content to display.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ConfigurationPreviewTheme(content: @Composable () -> Unit) {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false, isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}

/**
 * Composes the real wizard with fixed provider data and inert intents instead of application services.
 *
 * @param scenario Provider, wizard stage, validation and initial form position to display.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ServerSetupPreview(scenario: ServerSetupPreviewCase) {
    val sourceListState = rememberLazyListState(
        initialFirstVisibleItemIndex = scenario.state.visibleSources(ServerSetupStrings())
            .indexOf(scenario.state.mode)
            .coerceAtLeast(0),
    )
    val formListState =
        rememberLazyListState(initialFirstVisibleItemScrollOffset = scenario.formOffset)
    ConfigurationPreviewTheme {
        ServerSetupContent(
            state = scenario.state, processIntent = {}, modifier = Modifier.fillMaxSize(),
            sourceListState = sourceListState, configurationListState = formListState,
        )
    }
}
