package com.shifthackz.aisdv1.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

/**
 * Embeds WKWebView and publishes its history controller.
 * Navigation callbacks report actual page loading. Only a changed configured URL submits a new
 * request, so loading-state recomposition and in-page navigation preserve the current document.
 *
 * @param modifier Layout modifiers for the embedded browser viewport.
 * @param url Configured provider WebUI URL to load.
 * @param onLoadingChanged Reports navigation start, completion or failure to the route.
 * @param onControllerChanged Publishes native history controls, or null when the view is released.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun WebUiBrowser(
    modifier: Modifier,
    url: String,
    onLoadingChanged: (Boolean) -> Unit,
    onControllerChanged: (WebUiController?) -> Unit,
) {
    val controller = remember { IosWebUiController() }
    val currentOnLoadingChanged = rememberUpdatedState(onLoadingChanged)
    val currentOnControllerChanged = rememberUpdatedState(onControllerChanged)
    val navigationDelegate = remember {
        IosWebUiNavigationDelegate { currentOnLoadingChanged.value(it) }
    }
    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView().also { webView ->
                webView.navigationDelegate = navigationDelegate
                controller.webView = webView
                controller.requestedUrl = url
                currentOnControllerChanged.value(controller)
                webView.loadRequest(url.toRequest())
            }
        },
        update = { webView ->
            controller.webView = webView
            currentOnControllerChanged.value(controller)
            if (controller.requestedUrl != url) {
                controller.requestedUrl = url
                webView.loadRequest(url.toRequest())
            }
        },
        onRelease = { webView ->
            webView.navigationDelegate = null
            webView.stopLoading()
            controller.webView = null
            controller.requestedUrl = null
            currentOnControllerChanged.value(null)
        },
    )
}

/**
 * Leaves system-back handling to the iOS host; the toolbar retains browser-aware navigation.
 *
 * @param onBack Route callback accepted for platform API parity.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun WebUiBackHandler(
    onBack: () -> Unit,
) = Unit

/**
 * Retains the mounted browser for toolbar history navigation without owning its native lifetime.
 *
 * @author Dmitriy Moroz
 */
private class IosWebUiController : WebUiController {

    /**
     * Mounted browser; cleared before its native view is released.
     *
     * @author Dmitriy Moroz
     */
    var webView: WKWebView? = null

    /**
     * Last configured URL submitted, independent of redirects and browser history.
     *
     * @author Dmitriy Moroz
     */
    var requestedUrl: String? = null

    /**
     * Whether native history can handle Back before the route is dismissed.
     *
     * @author Dmitriy Moroz
     */
    override val canGoBack: Boolean
        get() = webView?.canGoBack ?: false

    /**
     * Returns to the previous document in the mounted browser, when available.
     *
     * @author Dmitriy Moroz
     */
    override fun goBack() {
        webView?.goBack()
    }
}

/**
 * Creates the configured page request, retaining the empty-page fallback for invalid addresses.
 *
 * @receiver Configured provider WebUI address.
 * @author Dmitriy Moroz
 */
private fun String.toRequest(): NSURLRequest =
    NSURL.URLWithString(this)?.let(::NSURLRequest)
        ?: NSURLRequest(NSURL.URLWithString("about:blank")!!)
