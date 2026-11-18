package com.shifthackz.aisdv1.data.mappers

import kotlin.time.Clock
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload

/**
 * Creates a gallery record from complete local PNG data and captured generation parameters.
 *
 * @receiver Immutable payload paired with complete Base64 PNG data.
 * @return Gallery record preserving the actual seed, dimensions, scheduler and steps.
 *
 * @author Dmitriy Moroz
 */
fun Pair<TextToImagePayload, String>.mapLocalDiffusionToAiGenResult(): AiGenerationResult =
    let { (payload, base64) ->
        AiGenerationResult(
            id = 0L,
            image = base64,
            inputImage = "",
            createdAt = Clock.System.now().toEpochMilliseconds(),
            type = AiGenerationResult.Type.TEXT_TO_IMAGE,
            denoisingStrength = 0f,
            prompt = payload.prompt,
            negativePrompt = payload.negativePrompt,
            width = payload.width,
            height = payload.height,
            samplingSteps = payload.samplingSteps,
            cfgScale = payload.cfgScale,
            restoreFaces = payload.restoreFaces,
            sampler = payload.sampler,
            seed = payload.seed,
            subSeed = payload.subSeed,
            subSeedStrength = payload.subSeedStrength,
            hidden = false,
        )
    }
