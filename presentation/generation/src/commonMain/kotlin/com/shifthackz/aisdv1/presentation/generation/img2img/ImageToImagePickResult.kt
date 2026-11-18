package com.shifthackz.aisdv1.presentation.generation.img2img

/**
 * Outcome of native selection and bounded source-image conversion.
 *
 * @author Dmitriy Moroz
 */
sealed interface ImageToImagePickResult {
    /**
     * A successfully admitted image ready for image-to-image editing.
     *
     * @param base64 Encoded original/cropped image produced by the native picker adapter.
     *
     * @author Dmitriy Moroz
     */
    data class Selected(val base64: String) : ImageToImagePickResult

    /**
     * The user dismissed the native picker without choosing an image.
     *
     * @author Dmitriy Moroz
     */
    data object Cancelled : ImageToImagePickResult

    /**
     * The current platform/adapter cannot provide the requested acquisition route.
     *
     * @author Dmitriy Moroz
     */
    data object Unsupported : ImageToImagePickResult

    /**
     * Selection or image conversion failed with public details.
     *
     * @param message User-visible explanation from the native adapter.
     *
     * @author Dmitriy Moroz
     */
    data class Failed(val message: String) : ImageToImagePickResult
}
