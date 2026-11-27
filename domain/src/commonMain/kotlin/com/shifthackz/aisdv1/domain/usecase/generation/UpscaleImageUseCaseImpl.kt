package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import com.shifthackz.aisdv1.domain.repository.ImageProcessingRepository
import com.shifthackz.aisdv1.domain.entity.MAX_UPSCALER_INPUT_SIZE
import com.shifthackz.aisdv1.domain.gateway.BenchmarkDeviceProbe
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.domain.gateway.UpscalerGateway

/**
 * Prepares bounded pixels and validates dedicated model ownership before native super-resolution.
 *
 * @param buildInfo Supplies the platform checked against the selected provider's availability.
 * @param models Resolves completed app-owned model paths.
 * @param images Downscales standalone input and reads generated pixels independently of provider metadata.
 * @param deviceProbe Captures device and OS for standalone upscale provenance.
 * @param gateway Platform inference owner; failures and cancellation propagate unchanged.
 * @author Dmitriy Moroz
 */
internal class UpscaleImageUseCaseImpl(
    private val models: DownloadableModelRepository,
    private val gateway: UpscalerGateway,
    private val images: ImageProcessingRepository,
    private val deviceProbe: BenchmarkDeviceProbe,
    private val buildInfo: BuildInfoProvider,
) : UpscaleImageUseCase {
    /**
     * Enlarges one image without replacing its original img2img input or generation provider.
     *
     * @param result Unsaved original generation image; request metadata dimensions are not used for admission.
     * @param configuration Captured model identity from upscaler setup.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(
        result: AiGenerationResult,
        configuration: UpscalerConfiguration
    ): AiGenerationResult {
        check(buildInfo.platform in configuration.source.allowedPlatforms) {
            "Local upscaling is unavailable on this platform"
        }
        require(UpscalerSource.forModel(configuration.modelId) == configuration.source) {
            "Upscaler model does not match the selected provider"
        }
        require(configuration.scaleFactor in configuration.source.supportedScaleFactors) { "Unsupported upscaler scale factor" }
        require(result.id == 0L) { "Upscaling requires an unsaved image" }
        val prepared = if (result.type == AiGenerationResult.Type.UPSCALE) {
            images.downscale(result.image, MAX_UPSCALER_INPUT_SIZE, MAX_UPSCALER_INPUT_SIZE)
        } else {
            images.read(result.image).also {
                require(it.width <= MAX_UPSCALER_INPUT_SIZE && it.height <= MAX_UPSCALER_INPUT_SIZE) {
                    "Generated image exceeds the enabled upscaler input size"
                }
            }
        }
        val device =
            if (result.type == AiGenerationResult.Type.UPSCALE) deviceProbe.capture() else null
        val model = models.getById(configuration.modelId)
        require(model.type == LocalAiModel.Type.Upscaler && model.downloaded) { "Download an upscaler model first" }
        val path = models.getModelPath(model)
        val nativeImage = gateway.upscale(prepared.image, path, prepared.width, prepared.height)
        val width = prepared.width * configuration.scaleFactor
        val height = prepared.height * configuration.scaleFactor
        val image =
            if (configuration.scaleFactor == configuration.source.nativeScaleFactor) nativeImage
            else images.downscale(nativeImage, width, height).also {
                check(it.width == width && it.height == height) { "Unexpected upscaler output dimensions" }
            }.image
        return result.copy(
            image = image, width = width, height = height,
            deviceName = device?.generationDeviceName() ?: result.deviceName,
            osVersion = device?.generationOsVersion() ?: result.osVersion,
            upscalerModelName = model.name, imageRevision = ""
        )
    }
}
