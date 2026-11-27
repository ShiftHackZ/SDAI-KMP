package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ProcessedImage
import com.shifthackz.aisdv1.domain.repository.ImageProcessingRepository

/**
 * Reads standalone image headers through the strict image-processing repository.
 *
 * @param repository Probes original dimensions without altering the image.
 * @author Dmitriy Moroz
 */
internal class ReadUpscalerImageUseCaseImpl(private val repository: ImageProcessingRepository) :
    ReadUpscalerImageUseCase {
    /**
     * Retains original bytes and propagates invalid-image errors to the acquisition flow.
     *
     * @param image Original Base64 pixels.
     * @author Dmitriy Moroz
     */
    override fun invoke(image: String): ProcessedImage = repository.read(image)
}
