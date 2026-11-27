package com.shifthackz.aisdv1.presentation.generation.img2img

/**
 * Returns unsupported selection without opening native UI.
 *
 * @author Dmitriy Moroz
 */
internal data object NoOpImageToImagePlatformActions : ImageToImagePlatformActions {

    /**
     * Reports unavailable acquisition without modifying input or opening platform UI.
     *
     * @param source Requested native acquisition route.
     * @param shouldCropToSquare Requested normalization policy, unused when acquisition is unavailable.
     * @author Dmitriy Moroz
     */
    override suspend fun pickImage(
        source: ImageToImagePickSource,
        shouldCropToSquare: Boolean
    ): ImageToImagePickResult =
        ImageToImagePickResult.Unsupported
}
