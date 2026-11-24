package com.shifthackz.aisdv1.domain.entity

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.platform.PlatformValue

/**
 * Independent super-resolution providers, separate from image generation sources.
 *
 * @param key Stable provider identity stored with standalone Gallery results.
 * @param type Hosting category used by provider discovery.
 * @param version Last meaningful implementation update in yyyy.M.d format.
 * @param readiness Platform-specific integration maturity displayed in configuration.
 * @param featureTags Super-resolution capabilities displayed and searched in provider discovery.
 * @param allowedPlatforms Platforms on which this provider can be selected and executed.
 * @param nativeScaleFactor Enlargement produced by the provider's supported model family.
 * @param supportedScaleFactors Final output factors offered after native inference and optional downsampling.
 * @author Dmitriy Moroz
 */
enum class UpscalerSource(
    val key: String,
    val version: String,
    val readiness: PlatformValue<ServerSourceReadiness>,
    val featureTags: Set<UpscalerFeatureTag>,
    val allowedPlatforms: Set<Platform>,
    val nativeScaleFactor: Int,
    val supportedScaleFactors: Set<Int>,
    val type: ServerSourceType = ServerSourceType.LOCAL,
) {
    LOCAL_ESRGAN(
        key = "local_esrgan",
        version = "2026.11.24",
        readiness = PlatformValue(ServerSourceReadiness.STABLE),
        featureTags = setOf(
            UpscalerFeatureTag.Offline,
            UpscalerFeatureTag.MultipleModels,
            UpscalerFeatureTag.TiledInference
        ),
        allowedPlatforms = setOf(Platform.ANDROID),
        nativeScaleFactor = 4,
        supportedScaleFactors = setOf(2, 4),
    ),
    LOCAL_MNN(
        key = "local_mnn_upscaler",
        version = "2026.11.24",
        readiness = PlatformValue(ServerSourceReadiness.STABLE),
        featureTags = setOf(UpscalerFeatureTag.Offline, UpscalerFeatureTag.TiledInference),
        allowedPlatforms = setOf(Platform.ANDROID, Platform.IOS),
        nativeScaleFactor = 4,
        supportedScaleFactors = setOf(2, 4),
    );

    /**
     * Catalog identity and platform availability shared by configuration and execution.
     *
     * @author Dmitriy Moroz
     */
    companion object {
        /**
         * Returns providers whose native implementation is available on the requested platform.
         *
         * @param platform Host running the application.
         * @author Dmitriy Moroz
         */
        fun availableOn(platform: Platform): List<UpscalerSource> =
            entries.filter { platform in it.allowedPlatforms }

        /**
         * Resolves the runtime family from the stable catalog model identity.
         *
         * @param modelId Dedicated catalog model identifier.
         * @author Dmitriy Moroz
         */
        fun forModel(modelId: String): UpscalerSource =
            if (modelId.startsWith("upscaler_mnn_")) LOCAL_MNN else LOCAL_ESRGAN
    }
}
