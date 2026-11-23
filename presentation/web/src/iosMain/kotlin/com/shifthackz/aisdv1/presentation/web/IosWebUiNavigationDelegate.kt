package com.shifthackz.aisdv1.presentation.web

import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSError
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

/**
 * Reports main-frame navigation activity for the toolbar progress indicator.
 * Completion from an older navigation cannot hide progress for a newer document load.
 * The Compose browser owner retains this delegate because WebKit holds it weakly.
 *
 * @param onLoadingChanged Receives true on navigation start and false on its completion or failure.
 * @author Dmitriy Moroz
 */
internal class IosWebUiNavigationDelegate(
    private val onLoadingChanged: (Boolean) -> Unit,
) : NSObject(), WKNavigationDelegateProtocol {
    private var activeNavigation: WKNavigation? = null

    /**
     * Starts progress for the document now being requested, including links and browser history.
     *
     * @param webView Browser reporting the navigation.
     * @param didStartProvisionalNavigation Identity of the newly started document load.
     * @author Dmitriy Moroz
     */
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
        activeNavigation = didStartProvisionalNavigation
        onLoadingChanged(true)
    }

    /**
     * Ends progress after the active document finishes loading.
     *
     * @param webView Browser reporting completion.
     * @param didFinishNavigation Identity of the finished document load.
     * @author Dmitriy Moroz
     */
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        finishNavigation(didFinishNavigation)
    }

    /**
     * Ends progress when the active document fails after navigation has committed.
     *
     * @param webView Browser reporting the failure.
     * @param didFailNavigation Identity of the failed document load.
     * @param withError Native navigation error supplied by WebKit.
     * @author Dmitriy Moroz
     */
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailNavigation: WKNavigation?, withError: NSError) {
        finishNavigation(didFailNavigation)
    }

    /**
     * Ends progress when the active request fails before a document can be committed.
     *
     * @param webView Browser reporting the failure.
     * @param didFailProvisionalNavigation Identity of the failed provisional load.
     * @param withError Native connection or navigation error supplied by WebKit.
     * @author Dmitriy Moroz
     */
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        finishNavigation(didFailProvisionalNavigation)
    }

    /**
     * Clears the indicator only for the latest navigation, retaining progress across replaced loads.
     *
     * @param navigation Identity supplied by a terminal WebKit callback.
     * @author Dmitriy Moroz
     */
    private fun finishNavigation(navigation: WKNavigation?) {
        if (activeNavigation == navigation) {
            activeNavigation = null
            onLoadingChanged(false)
        }
    }
}
