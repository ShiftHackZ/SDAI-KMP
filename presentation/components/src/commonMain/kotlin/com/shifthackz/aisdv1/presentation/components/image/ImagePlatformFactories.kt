package com.shifthackz.aisdv1.presentation.components.image

/**
 * Creates the platform default saver before host-specific DI overrides are installed.
 * Android supplies an unsupported default and installs its media gateway adapter through DI.
 *
 * @author Dmitriy Moroz
 */
expect fun createPlatformImageSaver(): ImageSaver

/**
 * Creates the platform default sharer before host-specific DI overrides are installed.
 * Android supplies an unsupported default and installs its file-provider adapter through DI.
 *
 * @author Dmitriy Moroz
 */
expect fun createPlatformImageSharer(): ImageSharer
