package com.shifthackz.aisdv1.presentation.generation.img2img

/**
 * Native camera/gallery selection that returns an admitted original image for image-to-image editing.
 *
 * @author Dmitriy Moroz
 */
interface ImageToImagePlatformActions {
    /**
     * Opens the requested native picker and converts its result within the original-image budget.
     *
     * @param source Camera capture or existing-photo selection requested by the user.
     * @param shouldCropToSquare Applies img2img square normalization; false preserves the complete aspect ratio.
     *
     * @return Selected encoded pixels, user cancellation, unsupported capability or conversion failure.
     *
     * @author Dmitriy Moroz
     */
    suspend fun pickImage(
        source: ImageToImagePickSource,
        shouldCropToSquare: Boolean = true
    ): ImageToImagePickResult
}
