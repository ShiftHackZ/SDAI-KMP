package com.shifthackz.aisdv1.presentation.components.core

/**
 * Reports background-generation support and presents platform feedback for foreground outcomes.
 *
 * @author Dmitriy Moroz
 */
interface GenerationPlatformServices {
    /**
     * Whether generation can be submitted through the platform background-work integration.
     *
     * @author Dmitriy Moroz
     */
    val supportsBackgroundGeneration: Boolean

    /**
     * Presents the platform foreground-generation success feedback.
     *
     * @author Dmitriy Moroz
     */
    fun showGenerationSucceeded()

    /**
     * Presents the platform foreground-generation failure feedback.
     *
     * @author Dmitriy Moroz
     */
    fun showGenerationFailed()
}
