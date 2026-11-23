package com.shifthackz.aisdv1.presentation.web.preview

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.web.WebUiContent
import com.shifthackz.aisdv1.presentation.web.WebUiState

/**
 * Shows configuration-loading feedback before the provider title and browser are available.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Configuration loading · Light", group = "WebUI", widthDp = 360, heightDp = 740)
@Preview(
    name = "Configuration loading · Dark", group = "WebUI", widthDp = 360, heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun WebUiConfigurationLoadingPreview() {
    WebUiPreview(state = WebUiState(), isPageLoading = true)
}

/**
 * Shows the provider toolbar and linear progress while the configured page is loading.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Page loading · Light", group = "WebUI", widthDp = 360, heightDp = 740)
@Preview(
    name = "Page loading · Dark", group = "WebUI", widthDp = 360, heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun WebUiPageLoadingPreview() {
    WebUiPreview(
        state = WebUiState(loading = false, url = "http://localhost:7860"),
        isPageLoading = true,
    )
}

/**
 * Shows the loaded viewport without progress, including a narrow screen with enlarged text.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Page ready · Light", group = "WebUI", widthDp = 360, heightDp = 740)
@Preview(
    name = "Page ready · Dark", group = "WebUI", widthDp = 360, heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Page ready · Compact large text", group = "WebUI", widthDp = 320, heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun WebUiPageReadyPreview() {
    WebUiPreview(
        state = WebUiState(loading = false, url = "http://localhost:7860"),
        isPageLoading = false,
    )
}

/**
 * Shows the alternate provider title with the same browser viewport and navigation controls.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "SwarmUI · Light", group = "WebUI", widthDp = 360, heightDp = 740)
@Preview(
    name = "SwarmUI · Dark", group = "WebUI", widthDp = 360, heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun WebUiSwarmPreview() {
    WebUiPreview(
        state = WebUiState(
            loading = false,
            source = ServerSource.SWARM_UI,
            url = "http://localhost:7801"
        ),
        isPageLoading = false,
    )
}

/**
 * Applies the application theme to fixed route state without Koin or a native browser.
 * The neutral viewport marks where remote content appears; no URL is requested by the preview.
 *
 * @param state Provider identity, example URL and configuration-loading state to illustrate.
 * @param isPageLoading Whether to display the route's page-loading progress indicator.
 * @author Dmitriy Moroz
 */
@Composable
private fun WebUiPreview(state: WebUiState, isPageLoading: Boolean) {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            WebUiContent(
                state = state,
                isPageLoading = isPageLoading,
                onNavigateBack = {},
            ) { browserModifier ->
                Column(
                    modifier = browserModifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Web content",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = state.url,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
