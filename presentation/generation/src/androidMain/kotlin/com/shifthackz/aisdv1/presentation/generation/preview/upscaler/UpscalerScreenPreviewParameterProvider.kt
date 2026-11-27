package com.shifthackz.aisdv1.presentation.generation.preview.upscaler

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures

/**
 * Covers source acquisition, proportional output sizes, setup failures and operation feedback.
 *
 * @author Dmitriy Moroz
 */
internal class UpscalerScreenPreviewParameterProvider :
    PreviewParameterProvider<UpscalerScreenPreviewCase> {
    private val ready = UpscalerPreviewFixtures.screen
    private val form = UpscalerPreviewFixtures.form
    private val result = GenerationPreviewFixtures.result.copy(
        type = AiGenerationResult.Type.UPSCALE, width = 2048, height = 2048,
        inputImage = GenerationPreviewFixtures.ImageBase64,
    )
    private val cases = listOf(
        UpscalerScreenPreviewCase(
            "Empty input",
            ready.copy(image = "", width = 0, height = 0, outputSize = null)
        ),
        UpscalerScreenPreviewCase("Square input · x4"),
        UpscalerScreenPreviewCase(
            "Portrait input",
            ready.copy(width = 768, height = 1024, outputSize = UpscalerImageSize(3072, 4096)),
            firstVisibleItemIndex = 1
        ),
        UpscalerScreenPreviewCase(
            "Landscape input",
            ready.copy(width = 1024, height = 512, outputSize = UpscalerImageSize(4096, 2048)),
            firstVisibleItemIndex = 1
        ),
        UpscalerScreenPreviewCase(
            "Parameters · x2",
            ready.copy(
                configuration = ready.configuration?.copy(scaleFactor = 2),
                outputSize = UpscalerImageSize(1024, 1024)
            ),
            firstVisibleItemIndex = 2
        ),
        UpscalerScreenPreviewCase("Parameters · x4", firstVisibleItemIndex = 2),
        UpscalerScreenPreviewCase(
            "MNN · iOS metadata",
            ready.copy(
                platform = Platform.IOS,
                configuration = UpscalerConfiguration(UpscalerPreviewFixtures.mnn.id)
            ),
            form.copy(
                platform = Platform.IOS,
                model = UpscalerPreviewFixtures.mnn,
                models = listOf(UpscalerPreviewFixtures.mnn)
            )
        ),
        UpscalerScreenPreviewCase(
            "Acquiring image",
            ready.copy(isPickingImage = true),
            shouldShowImage = false
        ),
        UpscalerScreenPreviewCase("Image preview loading", shouldShowImage = false),
        UpscalerScreenPreviewCase(
            "Models not configured",
            ready.copy(configuration = null, outputSize = null),
            form.copy(model = null, models = emptyList()),
            firstVisibleItemIndex = 2
        ),
        UpscalerScreenPreviewCase(
            "Catalog loading",
            ready.copy(configuration = null),
            form.copy(isLoading = true, model = null, models = emptyList()),
            firstVisibleItemIndex = 2
        ),
        UpscalerScreenPreviewCase(
            "Configuration error",
            formState = form.copy(error = "The model catalog could not be loaded. Open upscaler configuration to retry."),
            firstVisibleItemIndex = 2
        ),
        UpscalerScreenPreviewCase(
            "Unsupported provider",
            ready.copy(isSupported = false, configuration = null),
            form.copy(isSupported = false)
        ),
        UpscalerScreenPreviewCase(
            "Oversized input warning",
            ready.copy(
                width = 2048,
                height = 1536,
                outputSize = UpscalerImageSize(4096, 3072),
                hasDownscaleWarning = true
            )
        ),
        UpscalerScreenPreviewCase(
            "Preparing model",
            ready.copy(
                isRunning = true,
                screenModal = GenerationModal.Generating(
                    canCancel = true,
                    nativeEngine = NativeGenerationEngine.UPSCALER,
                    nativeStage = NativeGenerationStage.PREPARING
                )
            )
        ),
        UpscalerScreenPreviewCase(
            "Upscaling · 45 percent",
            ready.copy(
                isRunning = true,
                screenModal = GenerationModal.Generating(
                    canCancel = true,
                    nativeEngine = NativeGenerationEngine.UPSCALER,
                    nativeStage = NativeGenerationStage.RUNNING,
                    status = LocalDiffusionStatus(45, 100),
                    isOverallProgress = true
                )
            )
        ),
        UpscalerScreenPreviewCase(
            "Operation failed",
            ready.copy(screenModal = GenerationModal.Error("The upscaler could not process this image. Please try a smaller input.".asUiText()))
        ),
        UpscalerScreenPreviewCase(
            "Result · manual save",
            ready.copy(
                screenModal = GenerationModal.Image.Single(
                    result,
                    autoSaveEnabled = false,
                    reportEnabled = false
                )
            )
        ),
        UpscalerScreenPreviewCase(
            "Result · saved",
            ready.copy(
                screenModal = GenerationModal.Image.Single(
                    result,
                    autoSaveEnabled = true,
                    reportEnabled = false
                )
            )
        ),
    )

    /**
     * Fixed standalone states; composing them never dispatches application intents.
     *
     * @author Dmitriy Moroz
     */
    override val values = cases.asSequence()

    /**
     * Names the source, setup or operation state in the preview picker.
     *
     * @param index Position in the ordered snapshot list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
