package com.shifthackz.aisdv1.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.router.WebUiRouter
import com.shifthackz.aisdv1.domain.entity.ServerSource
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

/**
 * Binds the configured provider WebUI route to its remembered ViewModel from the initialized graph.
 *
 * @param modifier Layout modifiers for the WebUI screen.
 * @param router Optional route owner; defaults to the application graph binding.
 * @author Dmitriy Moroz
 */
@Composable
fun WebUiScreen(
    modifier: Modifier = Modifier,
    router: WebUiRouter? = null,
) {
    val koin = remember { getKoin() }
    val resolvedRouter = remember(koin, router) {
        router ?: koin.get<WebUiRouter>()
    }
    val viewModel = remember(koin, resolvedRouter) {
        koin.get<WebUiViewModel> {
            parametersOf(resolvedRouter)
        }
    }
    MviComponent(
        viewModel = viewModel,
    ) { state, intentHandler ->
        WebUiScreenContent(
            state = state,
            processIntent = intentHandler,
            modifier = modifier,
        )
    }
}

/**
 * Embeds the configured provider page with loading feedback and browser-aware back navigation.
 * SwarmUI returns directly to the app; other pages use native browser history before leaving the route.
 *
 * @param modifier Layout modifiers for the WebUI scaffold.
 * @param state Provider identity, configured URL and configuration-loading state.
 * @param processIntent Requests return to the previous application route.
 * @author Dmitriy Moroz
 */
@Composable
fun WebUiScreenContent(
    modifier: Modifier = Modifier,
    state: WebUiState = WebUiState(),
    processIntent: (WebUiIntent) -> Unit = {},
) {
    var isPageLoading by remember { mutableStateOf(true) }
    var webController: WebUiController? by remember { mutableStateOf(null) }

    fun navigateBack() {
        if (state.loading || state.source == ServerSource.SWARM_UI) {
            processIntent(WebUiIntent.NavigateBack)
            return
        }
        val controller = webController
        if (controller?.canGoBack == true) {
            controller.goBack()
        } else {
            processIntent(WebUiIntent.NavigateBack)
        }
    }

    WebUiBackHandler {
        navigateBack()
    }
    WebUiContent(
        state = state,
        isPageLoading = isPageLoading,
        onNavigateBack = ::navigateBack,
        modifier = modifier,
    ) { browserModifier ->
        state.url.takeIf(String::isNotBlank)?.let { url ->
            WebUiBrowser(
                modifier = browserModifier,
                url = url,
                onLoadingChanged = { isPageLoading = it },
                onControllerChanged = { webController = it },
            )
        }
    }
}

/**
 * Embeds the platform browser and reports its loading state and history controller.
 *
 * @param modifier Layout modifiers for the embedded browser viewport.
 * @param url Configured provider WebUI URL to load.
 * @param onLoadingChanged Reports platform adapter loading activity to the route.
 * @param onControllerChanged Publishes native history controls after the browser view is available.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun WebUiBrowser(
    modifier: Modifier = Modifier,
    url: String,
    onLoadingChanged: (Boolean) -> Unit = {},
    onControllerChanged: (WebUiController?) -> Unit = {},
)

/**
 * Installs platform system-back handling for the embedded browser route.
 *
 * @param onBack Requests browser-history navigation or application-route dismissal.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun WebUiBackHandler(
    onBack: () -> Unit,
)
