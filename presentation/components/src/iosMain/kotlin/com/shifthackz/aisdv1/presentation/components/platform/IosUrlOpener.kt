package com.shifthackz.aisdv1.presentation.components.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * Dispatches external destinations to UIApplication after escaping spaces in the supplied URL.
 *
 * @author Dmitriy Moroz
 */
object IosUrlOpener {

    /**
     * Opens a valid URL through the registered iOS application handler; malformed URLs are ignored.
     *
     * @param url Absolute external destination; spaces are percent-escaped before native parsing.
     *
     * @author Dmitriy Moroz
     */
    fun openUrl(url: String) {
        NSURL.URLWithString(url.sanitizeUrl())?.let { nsUrl ->
            UIApplication.sharedApplication.openURL(
                nsUrl,
                options = emptyMap<Any?, Any?>(),
                completionHandler = null,
            )
        }
    }

    private fun String.sanitizeUrl(): String = replace(" ", "%20")
}
