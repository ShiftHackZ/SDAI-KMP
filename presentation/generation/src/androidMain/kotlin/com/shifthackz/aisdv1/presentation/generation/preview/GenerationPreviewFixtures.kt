package com.shifthackz.aisdv1.presentation.generation.preview

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.PerformanceSample
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiSampler
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionState
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageInPaintState
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageState
import com.shifthackz.aisdv1.presentation.generation.img2img.InPaintPoint
import com.shifthackz.aisdv1.presentation.generation.img2img.InPaintStroke
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageState

/**
 * Fixed generation inputs, fictional model choices and tiny checkerboard pixels without app services.
 *
 * @author Dmitriy Moroz
 */
internal object GenerationPreviewFixtures {
    /**
     * A bundled 32-pixel checkerboard PNG decoded synchronously by the Android preview wrapper.
     *
     * @author Dmitriy Moroz
     */
    const val ImageBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAQklEQVR4nGO4c+IrVpQXugorIlU9w6gFoxYMAQuoZRAu9aMWjFowFCyglkG41I9aMGrBULCAWgbhUj9qwagFQ8ACAP32DHmKhrEjAAAAAElFTkSuQmCC"

    val mask = ImageInPaintState(
        strokes = listOf(
            InPaintStroke(
                points = listOf(
                    InPaintPoint(70f, 75f),
                    InPaintPoint(160f, 130f),
                    InPaintPoint(230f, 200f)
                ),
                brushSize = 32, canvasWidth = 320, canvasHeight = 320,
            ),
        )
    )
    val result = AiGenerationResult(
        id = 1L,
        image = ImageBase64,
        inputImage = "",
        createdAt = 1_700_000_000_000L,
        type = AiGenerationResult.Type.TEXT_TO_IMAGE,
        prompt = "A mountain village at sunset",
        negativePrompt = "blurry",
        width = 512,
        height = 512,
        samplingSteps = 20,
        cfgScale = 7f,
        restoreFaces = false,
        sampler = "Euler",
        seed = "123456789",
        subSeed = "",
        subSeedStrength = 0f,
        denoisingStrength = 0.75f,
        hidden = false,
        modelName = "Sample diffusion",
        imageRevision = "generation-preview-1",
    )
    val samples = List(60) { index ->
        PerformanceSample(
            cpuPercent = 20f + index % 25,
            deviceCpuPercent = 45f + index % 30,
            residentBytes = (900L + index * 5L) * 1_048_576,
            deviceUsedMemoryBytes = (3000L + index * 8L) * 1_048_576,
            totalMemoryBytes = 8L * 1024 * 1_048_576,
        )
    }

    /**
     * Creates populated text inputs for one actual provider with platform-appropriate controls.
     *
     * @param source Provider selecting the form's capability layout.
     * @author Dmitriy Moroz
     */
    fun text(source: ServerSource = ServerSource.AUTOMATIC1111) = TextToImageState(
        loadingConfiguration = false,
        mode = source,
        platform = if (source == ServerSource.LOCAL_APPLE_CORE_ML || source == ServerSource.LOCAL_APPLE_BONSAI) Platform.IOS else Platform.ANDROID,
        prompt = "A mountain village at sunset, watercolor, warm window lights",
        negativePrompt = "low quality, blurry",
        seed = "123456789",
        availableSamplers = if (source == ServerSource.STABILITY_AI) StabilityAiSampler.entries.map { it.toString() } else listOf(
            "Euler",
            "Euler a",
            "DPM++ 2M"
        ),
        selectedSampler = if (source == ServerSource.STABILITY_AI) StabilityAiSampler.entries.first()
            .toString() else "Euler",
        arliAiModels = listOf("sample-diffusion", "sample-fast-model"),
        arliAiModel = "sample-diffusion",
    )

    /**
     * Creates populated image inputs for a provider; source acquisition remains unstarted.
     *
     * @param source Provider selecting supported/unsupported source-image controls.
     * @author Dmitriy Moroz
     */
    fun image(source: ServerSource = ServerSource.AUTOMATIC1111) = ImageToImageState(
        loadingConfiguration = false,
        mode = source,
        platform = if (source == ServerSource.LOCAL_APPLE_CORE_ML) Platform.IOS else Platform.ANDROID,
        prompt = "Restore the village details",
        negativePrompt = "artifacts",
        seed = "123456789",
        availableSamplers = if (source == ServerSource.STABILITY_AI) StabilityAiSampler.entries.map { it.toString() } else listOf(
            "Euler",
            "Euler a",
            "DPM++ 2M"
        ),
        selectedSampler = if (source == ServerSource.STABILITY_AI) StabilityAiSampler.entries.first()
            .toString() else "Euler",
        arliAiModels = listOf("sample-diffusion", "sample-fast-model"),
        arliAiModel = "sample-diffusion",
    )

    /**
     * Supplies a provider-specific model picker snapshot without observing saved configuration.
     *
     * @param source Provider whose checkpoint picker is shown.
     * @author Dmitriy Moroz
     */
    fun engine(source: ServerSource): EngineSelectionState {
        val model = LocalAiModel(
            id = "sample-model",
            type = when (source) {
                ServerSource.LOCAL_MNN -> LocalAiModel.Type.Mnn
                ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> LocalAiModel.Type.Sdxl
                ServerSource.LOCAL_APPLE_CORE_ML -> LocalAiModel.Type.CoreMl
                ServerSource.LOCAL_APPLE_BONSAI -> LocalAiModel.Type.Bonsai
                else -> LocalAiModel.Type.ONNX
            },
            name = "Sample diffusion",
            size = "1 GB",
            sources = emptyList(),
            downloaded = true,
            selected = true,
        )
        return EngineSelectionState(
            loading = false,
            mode = source,
            sdModels = listOf("Sample diffusion", "Sample illustration"),
            selectedSdModel = "Sample diffusion",
            swarmModels = listOf("Sample diffusion"),
            selectedSwarmModel = "Sample diffusion",
            easyDiffusionModels = listOf(EasyDiffusionModel("sample-model", "Sample diffusion")),
            selectedEasyDiffusionModel = "sample-model",
            hfModels = listOf("sample/model"),
            selectedHfModel = "sample/model",
            stEngines = listOf("sample-diffusion"),
            selectedStEngine = "sample-diffusion",
            localAiModels = listOf(model),
            selectedLocalAiModelId = model.id,
        )
    }
}
