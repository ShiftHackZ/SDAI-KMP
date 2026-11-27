package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ProcessedImage

/**
 * Reads authoritative standalone input dimensions while retaining the original encoded image.
 *
 * @author Dmitriy Moroz
 */
interface ReadUpscalerImageUseCase {
    /**
     * Validates encoded-image headers without resizing or allocating the original pixel raster.
     *
     * @param image Original Base64 input from standalone image selection.
     * @return Unmodified encoded image and its actual pixel dimensions.
     * @author Dmitriy Moroz
     */
    operator fun invoke(image: String): ProcessedImage
}
