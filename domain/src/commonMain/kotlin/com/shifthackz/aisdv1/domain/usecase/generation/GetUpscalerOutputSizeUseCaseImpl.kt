package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.MAX_UPSCALER_INPUT_SIZE
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize

/**
 * Uses integer aspect-ratio arithmetic so the limiting input edge remains exactly 1024 pixels.
 *
 * @author Dmitriy Moroz
 */
internal class GetUpscalerOutputSizeUseCaseImpl : GetUpscalerOutputSizeUseCase {
    /**
     * Leaves smaller inputs at their native size and scales the fitted dimensions by the selected factor.
     *
     * @param width Actual input width, or zero before selection.
     * @param height Actual input height, or zero before selection.
     * @param configuration Current provider/model/final scale.
     * @author Dmitriy Moroz
     */
    override fun invoke(
        width: Int,
        height: Int,
        configuration: UpscalerConfiguration?
    ): UpscalerImageSize? {
        if (width <= 0 || height <= 0 || configuration == null) return null
        require(configuration.scaleFactor in configuration.source.supportedScaleFactors)
        val edge = MAX_UPSCALER_INPUT_SIZE
        val (preparedWidth, preparedHeight) = when {
            width <= edge && height <= edge -> width to height
            width >= height -> edge to (height.toLong() * edge / width).toInt().coerceAtLeast(1)
            else -> (width.toLong() * edge / height).toInt().coerceAtLeast(1) to edge
        }
        return UpscalerImageSize(
            preparedWidth * configuration.scaleFactor,
            preparedHeight * configuration.scaleFactor
        )
    }
}
