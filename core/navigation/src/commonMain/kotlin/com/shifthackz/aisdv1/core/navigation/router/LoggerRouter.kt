package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation actions consumed by the logger screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface LoggerRouter {
    /**
     * Closes the drawer first, otherwise returns to the previous route or the generation-home fallback.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()
}
