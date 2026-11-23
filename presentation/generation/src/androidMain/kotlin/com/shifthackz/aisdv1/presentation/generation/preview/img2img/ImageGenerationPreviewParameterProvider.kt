package com.shifthackz.aisdv1.presentation.generation.preview.img2img

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageState

/**
 * Supplies supported and unsupported providers, source acquisition, masks and form feedback.
 *
 * @author Dmitriy Moroz
 */
internal class ImageGenerationPreviewParameterProvider :
    PreviewParameterProvider<ImageGenerationPreviewCase> {
    private val empty = GenerationPreviewFixtures.image()
    private val selected = empty.copy(imageBase64 = GenerationPreviewFixtures.ImageBase64)
    private val advanced = selected.copy(advancedOptionsVisible = true)
    private val cases = ServerSource.entries.map { source ->
        ImageGenerationPreviewCase(
            "Provider · ${source.name}",
            GenerationPreviewFixtures.image(source)
        )
    } + listOf(
        ImageGenerationPreviewCase("Configuration loading", ImageToImageState()),
        ImageGenerationPreviewCase("Acquiring image", empty.copy(pickingImage = true)),
        ImageGenerationPreviewCase("Selected source", selected),
        ImageGenerationPreviewCase("Source unavailable", selected, isSourceUnavailable = true),
        ImageGenerationPreviewCase(
            "Source with mask",
            selected.copy(inPaint = GenerationPreviewFixtures.mask)
        ),
        ImageGenerationPreviewCase(
            "Retained source while acquiring",
            selected.copy(pickingImage = true)
        ),
        ImageGenerationPreviewCase(
            "Core ML source without inpaint",
            GenerationPreviewFixtures.image(ServerSource.LOCAL_APPLE_CORE_ML)
                .copy(imageBase64 = GenerationPreviewFixtures.ImageBase64)
        ),
        ImageGenerationPreviewCase(
            "Easy Diffusion source without inpaint",
            GenerationPreviewFixtures.image(ServerSource.EASY_DIFFUSION)
                .copy(imageBase64 = GenerationPreviewFixtures.ImageBase64)
        ),
        ImageGenerationPreviewCase(
            "Options collapsed",
            selected,
            anchor = "header",
            isFormExpanded = false
        ),
        ImageGenerationPreviewCase(
            "Tagged prompts",
            selected.copy(
                formPromptTaggedInput = true,
                prompt = "village, watercolor, <lora:sample:0.8>"
            ),
            anchor = "base-PROMPT"
        ),
        ImageGenerationPreviewCase(
            "Denoising control",
            advanced,
            anchor = "advanced-AFTER_SLIDERS"
        ),
        ImageGenerationPreviewCase(
            "Dimensions invalid",
            selected.copy(
                width = "invalid",
                widthValidationError = "Enter a valid width".asUiText()
            ),
            anchor = "base-DIMENSIONS"
        ),
        ImageGenerationPreviewCase(
            "Seed invalid",
            advanced.copy(seed = "invalid", seedValidationError = "Enter a valid seed".asUiText()),
            anchor = "advanced-SEED"
        ),
        ImageGenerationPreviewCase(
            "Prompt validation",
            selected.copy(prompt = "", promptValidationError = "Enter a prompt before generating."),
            anchor = "footer"
        ),
        ImageGenerationPreviewCase(
            "Generating inline status",
            selected.copy(generating = true),
            anchor = "footer"
        ),
        ImageGenerationPreviewCase(
            "Inline error",
            selected.copy(error = "The source image could not be processed."),
            anchor = "footer"
        ),
        ImageGenerationPreviewCase(
            "Inline confirmation",
            selected.copy(message = "Image saved to the gallery."),
            anchor = "footer"
        ),
        ImageGenerationPreviewCase(
            "Sampler retry",
            advanced.copy(
                hasSamplerLoadFailed = true,
                availableSamplers = emptyList(),
                selectedSampler = ""
            ),
            anchor = "advanced-SAMPLER"
        ),
    ) + FalAiModel.imageToImage.map { model ->
        ImageGenerationPreviewCase(
            "Fal endpoint · ${model.name}",
            GenerationPreviewFixtures.image(ServerSource.FAL_AI).copy(
                falAiModel = model,
                samplingSteps = 20.coerceIn(model.minInferenceSteps, model.maxInferenceSteps),
                cfgScale = 7f.coerceIn(model.minGuidanceScale, model.maxGuidanceScale),
                falAiAcceleration = model.supportedAccelerations.first(),
            ),
            anchor = "base-PROVIDER"
        )
    }

    /**
     * Fixed source-form snapshots with inert acquisition, generation and navigation callbacks.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<ImageGenerationPreviewCase> = cases.asSequence()

    /**
     * Identifies the provider, source-image or form-status condition.
     *
     * @param index Position in the ordered source-form list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
