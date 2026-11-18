package com.shifthackz.aisdv1.presentation.components.core

/**
 * Disables background generation and suppresses outcome feedback on unsupported hosts.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpGenerationPlatformServices : GenerationPlatformServices {

    override val supportsBackgroundGeneration: Boolean = false

    override fun showGenerationSucceeded() = Unit

    override fun showGenerationFailed() = Unit
}
