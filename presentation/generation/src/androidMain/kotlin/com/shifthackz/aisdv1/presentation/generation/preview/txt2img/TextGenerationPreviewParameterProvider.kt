package com.shifthackz.aisdv1.presentation.generation.preview.txt2img

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageState
import com.shifthackz.aisdv1.presentation.generation.widget.input.GenerationFormField
import com.shifthackz.aisdv1.presentation.generation.widget.input.generationInputAdvancedFields
import com.shifthackz.aisdv1.presentation.generation.widget.input.toAdvancedOptionsState

/**
 * Supplies every provider layout, each logical advanced control and form validation/recovery states.
 *
 * @author Dmitriy Moroz
 */
internal class TextGenerationPreviewParameterProvider :
    PreviewParameterProvider<TextGenerationPreviewCase> {
    private val ready = GenerationPreviewFixtures.text()
    private val modules = listOf(
        ForgeModule("Sample VAE", "sample-vae"),
        ForgeModule("Sample text encoder", "sample-encoder")
    )
    private val extended = ready.copy(
        advancedOptionsVisible = true,
        hires = HiresConfig(enabled = true, steps = 10),
        aDetailer = ADetailerConfig(enabled = true), aDetailerAvailable = true,
        availableForgeModules = modules, selectedForgeModules = modules.take(1),
    )
    private val fieldStates = listOf(
        extended,
        GenerationPreviewFixtures.text(ServerSource.STABILITY_AI)
            .copy(advancedOptionsVisible = true),
        GenerationPreviewFixtures.text(ServerSource.FAL_AI).copy(advancedOptionsVisible = true),
    ).flatMap { state ->
        generationInputAdvancedFields(
            state.toAdvancedOptionsState(),
            false
        ).map { field -> state to field }
    }.filter { it.second != GenerationFormField.AFTER_SLIDERS }.distinctBy { it.second }
    private val cases = ServerSource.entries.map { source ->
        TextGenerationPreviewCase(
            "Provider · ${source.name}",
            GenerationPreviewFixtures.text(source)
        )
    } + fieldStates.map { (state, field) ->
        TextGenerationPreviewCase(
            "Advanced control · ${field.name}",
            state,
            anchor = "advanced-${field.name}"
        )
    } + listOf(
        TextGenerationPreviewCase("Configuration loading", TextToImageState()),
        TextGenerationPreviewCase("Empty prompt", ready.copy(prompt = "", negativePrompt = "")),
        TextGenerationPreviewCase(
            "Long prompt",
            ready.copy(
                prompt = "A detailed village, winding paths, warm lights and distant snowy peaks. ".repeat(
                    6
                )
            ),
            anchor = "base-PROMPT"
        ),
        TextGenerationPreviewCase(
            "Tagged prompts",
            ready.copy(
                formPromptTaggedInput = true,
                prompt = "village, watercolor, <lora:sample:0.8>",
                negativePrompt = "blurry, noisy"
            ),
            anchor = "base-PROMPT"
        ),
        TextGenerationPreviewCase("Options collapsed", ready, isFormExpanded = false),
        TextGenerationPreviewCase(
            "Options without separate advanced toggle",
            ready.copy(advancedToggleButtonVisible = false),
            isFormExpanded = false
        ),
        TextGenerationPreviewCase(
            "Dimension validation",
            ready.copy(
                width = "0",
                height = "invalid",
                widthValidationError = "Enter a supported width".asUiText(),
                heightValidationError = "Enter a supported height".asUiText()
            ),
            anchor = "base-DIMENSIONS"
        ),
        TextGenerationPreviewCase(
            "Seed validation",
            extended.copy(seed = "invalid", seedValidationError = "Enter a valid seed".asUiText()),
            anchor = "advanced-SEED"
        ),
        TextGenerationPreviewCase(
            "Variation seed validation",
            extended.copy(
                subSeed = "invalid",
                subSeedValidationError = "Enter a valid variation seed".asUiText()
            ),
            anchor = "advanced-VARIATION_SEED"
        ),
        TextGenerationPreviewCase(
            "Sampler loading",
            extended.copy(isLoadingSamplers = true),
            anchor = "advanced-SAMPLER"
        ),
        TextGenerationPreviewCase(
            "Sampler retry",
            extended.copy(
                hasSamplerLoadFailed = true,
                availableSamplers = emptyList(),
                selectedSampler = ""
            ),
            anchor = "advanced-SAMPLER"
        ),
        TextGenerationPreviewCase(
            "Sampler empty",
            extended.copy(availableSamplers = emptyList(), selectedSampler = ""),
            anchor = "advanced-SAMPLER"
        ),
        TextGenerationPreviewCase(
            "ADetailer not installed",
            extended.copy(aDetailerAvailable = false),
            anchor = "advanced-ADETAILER_MISSING"
        ),
        TextGenerationPreviewCase(
            "ADetailer refreshing",
            extended.copy(aDetailerAvailable = false, aDetailerRefreshing = true),
            anchor = "advanced-ADETAILER_MISSING"
        ),
        TextGenerationPreviewCase(
            "Generating inline status",
            ready.copy(generating = true),
            anchor = "footer"
        ),
        TextGenerationPreviewCase(
            "Inline error",
            ready.copy(error = "The provider could not accept this request.".asUiText()),
            anchor = "footer"
        ),
        TextGenerationPreviewCase(
            "Inline confirmation",
            ready.copy(message = "Image saved to the gallery.".asUiText()),
            anchor = "footer"
        ),
        TextGenerationPreviewCase(
            "Model discovery loading",
            ready,
            engineState = GenerationPreviewFixtures.engine(ready.mode).copy(loading = true)
        ),
        TextGenerationPreviewCase(
            "No model selected",
            ready,
            engineState = GenerationPreviewFixtures.engine(ready.mode)
                .copy(sdModels = emptyList(), selectedSdModel = "")
        ),
        TextGenerationPreviewCase(
            "Easy Diffusion model retry",
            GenerationPreviewFixtures.text(ServerSource.EASY_DIFFUSION),
            engineState = GenerationPreviewFixtures.engine(ServerSource.EASY_DIFFUSION).copy(
                easyDiffusionModels = emptyList(),
                selectedEasyDiffusionModel = "",
                hasEasyDiffusionModelLoadFailed = true
            )
        ),
        TextGenerationPreviewCase(
            "Compatible model and size validation",
            GenerationPreviewFixtures.text(ServerSource.OPEN_AI_COMPATIBLE).copy(
                openAiCompatibleModel = "sample-image-model",
                width = "invalid",
                widthValidationError = "Enter a valid image width".asUiText()
            ),
            anchor = "base-COMPATIBLE_DIMENSIONS"
        ),
        TextGenerationPreviewCase(
            "MNN Metal",
            GenerationPreviewFixtures.text(ServerSource.LOCAL_MNN).copy(
                platform = Platform.IOS,
                mnnBackend = MnnBackend.METAL,
                supportedMnnBackends = listOf(MnnBackend.CPU, MnnBackend.METAL)
            )
        ),
        TextGenerationPreviewCase(
            "MNN OpenCL",
            GenerationPreviewFixtures.text(ServerSource.LOCAL_MNN)
                .copy(mnnBackend = MnnBackend.OPENCL)
        ),
        TextGenerationPreviewCase(
            "Bonsai Android backend",
            GenerationPreviewFixtures.text(ServerSource.LOCAL_APPLE_BONSAI).copy(
                platform = Platform.ANDROID,
                bonsaiBackendSelectionVisible = true,
                bonsaiBackend = BonsaiBackend.AUTO
            )
        ),
        TextGenerationPreviewCase("Onboarding illustration", ready.copy(onBoardingDemo = true)),
    ) + OpenAiModel.entries.map { model ->
        TextGenerationPreviewCase(
            "OpenAI model · ${model.alias}",
            GenerationPreviewFixtures.text(ServerSource.OPEN_AI).copy(openAiModel = model),
            anchor = "base-DIMENSIONS"
        )
    } + FalAiModel.textToImage.map { model ->
        TextGenerationPreviewCase(
            "Fal endpoint · ${model.name}",
            GenerationPreviewFixtures.text(ServerSource.FAL_AI).copy(
                falAiModel = model,
                samplingSteps = 20.coerceIn(model.minInferenceSteps, model.maxInferenceSteps),
                cfgScale = 7f.coerceIn(model.minGuidanceScale, model.maxGuidanceScale),
                falAiAcceleration = model.supportedAccelerations.first(),
            )
        )
    } + SdxlBackend.entries.map { backend ->
        TextGenerationPreviewCase(
            "SDCPP backend · ${backend.name}",
            GenerationPreviewFixtures.text(ServerSource.LOCAL_STABLE_DIFFUSION_CPP)
                .copy(sdxlBackend = backend)
        )
    }

    /**
     * Fixed text-form snapshots composed with inert model and form callbacks.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<TextGenerationPreviewCase> = cases.asSequence()

    /**
     * Identifies the provider, logical control or validation/recovery condition.
     *
     * @param index Position in the ordered form-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
