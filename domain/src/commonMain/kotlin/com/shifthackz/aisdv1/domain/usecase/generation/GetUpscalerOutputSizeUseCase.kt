package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize

/**
 * Predicts standalone output size without decoding or modifying image pixels.
 *
 * @author Dmitriy Moroz
 */
interface GetUpscalerOutputSizeUseCase {
    /**
     * Accounts for proportional 1024px input preparation and the captured provider's final scale.
     *
     * @param width Actual selected input width, or zero without an input.
     * @param height Actual selected input height, or zero without an input.
     * @param configuration Captured provider, model and scale; null means setup is incomplete.
     * @return Expected output dimensions, or null until both input and configuration are available.
     * @author Dmitriy Moroz
     */
    operator fun invoke(
        width: Int,
        height: Int,
        configuration: UpscalerConfiguration?
    ): UpscalerImageSize?
}
