package com.shifthackz.aisdv1.domain.entity

/**
 * Super-resolution capabilities used for upscaler discovery independently of diffusion features.
 *
 * @author Dmitriy Moroz
 */
enum class UpscalerFeatureTag {
    Offline,
    MultipleModels,
    TiledInference,
}
