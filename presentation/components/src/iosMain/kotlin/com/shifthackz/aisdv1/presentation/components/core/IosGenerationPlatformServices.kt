package com.shifthackz.aisdv1.presentation.components.core

/**
 * Enables the shared iOS background-work control while leaving immediate outcome notifications to native integration.
 *
 * @author Dmitriy Moroz
 */
object IosGenerationPlatformServices : GenerationPlatformServices {

    override val supportsBackgroundGeneration: Boolean = true

    /**
     * Produces no additional foreground-success notification on iOS.
     *
     * @author Dmitriy Moroz
     */
    override fun showGenerationSucceeded() = Unit

    /**
     * Produces no additional foreground-failure notification on iOS.
     *
     * @author Dmitriy Moroz
     */
    override fun showGenerationFailed() = Unit
}
