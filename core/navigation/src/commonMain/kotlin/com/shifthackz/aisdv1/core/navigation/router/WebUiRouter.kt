package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation actions consumed by the web ui screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface WebUiRouter {
    /**
     * Closes the drawer first, otherwise returns to the previous route or the generation-home fallback.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()
}
