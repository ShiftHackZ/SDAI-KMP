package com.shifthackz.aisdv1.feature.civitai

import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASPresentationAnchor
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

/**
 * Resolves the active app window for the native Civitai sign-in sheet.
 *
 * @author Dmitriy Moroz
 */
internal class IosCivitaiOAuthPresentationContext : NSObject(),
    ASWebAuthenticationPresentationContextProvidingProtocol {
    /**
     * Anchors authentication to the current key window rather than creating another window.
     *
     * @param session System authentication session requesting its presentation owner.
     * @author Dmitriy Moroz
     */
    override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): ASPresentationAnchor =
        UIApplication.sharedApplication.connectedScenes.filterIsInstance<UIWindowScene>()
            .flatMap { it.windows }.filterIsInstance<UIWindow>().firstOrNull(UIWindow::isKeyWindow)
            ?: error("CIVITAI_AUTH_WINDOW_UNAVAILABLE")
}
