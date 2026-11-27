package com.shifthackz.aisdv1.domain.entity

/**
 * Captures the prepared image and expected fourfold output for either native host.
 *
 * @param image Base64 encoded source whose dimensions have been validated by the domain.
 * @param modelPath App-private checkpoint matching the selected upscaler provider.
 * @param width Prepared source width.
 * @param height Prepared source height.
 * @author Dmitriy Moroz
 */
fun upscalerNativeRequest(
    image: String,
    modelPath: String,
    width: Int,
    height: Int
): NativeGenerationRequest =
    NativeGenerationRequest(
        engine = NativeGenerationEngine.UPSCALER,
        modelPath = modelPath, upscaleImage = image,
        payload = TextToImagePayload(
            prompt = "", negativePrompt = "", samplingSteps = 1, cfgScale = 1f,
            width = width * 4, height = height * 4, restoreFaces = false,
            seed = "0", subSeed = "", subSeedStrength = 0f, sampler = "", nsfw = false,
            batchCount = 1, style = null, quality = null, openAiModel = null,
            stabilityAiClipGuidance = null, stabilityAiStylePreset = null,
        ),
    )
