package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation out of reusable backup screens without exposing application assembly.
 *
 * @author Dmitriy Moroz
 */
interface BackupRouter {
    /**
     * Returns to the preceding Settings route after normal-flow backup.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()

    /**
     * Consumes a reconstructed recovery route after provider setup has already completed.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToHomeScreen()

    /**
     * Replaces the invitation with the restore-only first-launch screen.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToInitialRestore()

    /**
     * Consumes first-launch recovery and opens provider selection/configuration.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToInitialConfiguration()
}
