package com.shifthackz.aisdv1.presentation.generation.img2img

/**
 * Returns unsupported selection without opening native UI.
 *
 * @author Dmitriy Moroz
 */
internal data object NoOpImageToImagePlatformActions : ImageToImagePlatformActions {

    override suspend fun pickImage(source: ImageToImagePickSource): ImageToImagePickResult =
        ImageToImagePickResult.Unsupported
}
