package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation actions consumed by the server setup screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface ServerSetupRouter {
    /**
     * Closes the drawer first, otherwise returns to the previous route or the generation-home fallback.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()

    /**
     * Replaces the completed setup route with configuration loading so Back cannot reopen setup.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToPostSetupConfigLoader()
}
