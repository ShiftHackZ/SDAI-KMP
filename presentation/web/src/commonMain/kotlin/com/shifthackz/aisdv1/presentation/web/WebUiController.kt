package com.shifthackz.aisdv1.presentation.web

/**
 * Native browser-history controls exposed to the shared WebUI route.
 *
 * @author Dmitriy Moroz
 */
interface WebUiController {
    /**
     * Whether the embedded page currently has a preceding browser-history entry.
     *
     * @author Dmitriy Moroz
     */
    val canGoBack: Boolean

    /**
     * Navigates to the preceding browser-history entry when the native view supports it.
     *
     * @author Dmitriy Moroz
     */
    fun goBack()
}
