package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.core.imageprocessing.ImageDownscaler
import com.shifthackz.aisdv1.data.image.BackupImageDecoder
import com.shifthackz.aisdv1.domain.entity.ProcessedImage
import com.shifthackz.aisdv1.domain.repository.ImageProcessingRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.io.encoding.Base64

/**
 * Adapts native encoded-image downscaling to the domain's prepared-image contract.
 *
 * @param downscaler Shared platform proportional downscaling implementation.
 * @param decoder Probes authoritative output headers without another raster allocation.
 * @author Dmitriy Moroz
 */
internal class ImageProcessingRepositoryImpl(
    private val downscaler: ImageDownscaler,
    private val decoder: BackupImageDecoder,
) : ImageProcessingRepository {
    /**
     * Probes original headers without rasterizing or replacing the encoded image.
     *
     * @param image Base64 original pixels.
     * @author Dmitriy Moroz
     */
    override fun read(image: String): ProcessedImage {
        val (width, height) = decoder.dimensions(Base64.decode(image))
        require(width in 1L..Int.MAX_VALUE.toLong() && height in 1L..Int.MAX_VALUE.toLong())
        return ProcessedImage(image, width.toInt(), height.toInt())
    }

    /**
     * Preserves unchanged encoded sources; downscaled sources are returned as lossless PNG.
     *
     * @param image Base64 original pixels.
     * @param maxWidth Positive maximum output width.
     * @param maxHeight Positive maximum output height.
     * @author Dmitriy Moroz
     */
    override suspend fun downscale(image: String, maxWidth: Int, maxHeight: Int): ProcessedImage {
        currentCoroutineContext().ensureActive()
        val bytes = Base64.decode(image)
        val downscaled = downscaler.downscale(bytes, maxWidth, maxHeight)
        currentCoroutineContext().ensureActive()
        val (width, height) = decoder.dimensions(downscaled)
        require(width in 1L..maxWidth.toLong() && height in 1L..maxHeight.toLong())
        return ProcessedImage(
            if (downscaled === bytes) image else Base64.encode(downscaled),
            width.toInt(),
            height.toInt()
        )
    }
}
