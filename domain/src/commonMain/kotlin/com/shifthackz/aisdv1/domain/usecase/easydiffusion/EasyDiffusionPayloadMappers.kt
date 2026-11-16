package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionConnection
import kotlin.random.Random
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

/** Captures one endpoint, checkpoint and source revision for the whole text batch.
 * @param connection Atomically captured endpoint, checkpoint and original submitted revision.
 * @author Dmitriy Moroz */
@OptIn(ExperimentalUuidApi::class)
internal fun TextToImagePayload.toEasyDiffusionRequest(connection: EasyDiffusionConnection): EasyDiffusionRequest {
    return EasyDiffusionRequest(
        url = connection.url,
        sessionId = Uuid.random().toString(),
        model = connection.model,
        prompt = prompt,
        negativePrompt = negativePrompt,
        width = width,
        height = height,
        steps = samplingSteps,
        cfgScale = cfgScale,
        seed = easyDiffusionSeed(seed),
        batchCount = batchCount,
        selectionRevision = connection.selectionRevision,
    )
}

/** Captures img2img inputs without inpaint, filters or hidden provider-specific fields.
 * @param connection Atomically captured endpoint, checkpoint and original submitted revision.
 * @author Dmitriy Moroz */
@OptIn(ExperimentalUuidApi::class)
internal fun ImageToImagePayload.toEasyDiffusionRequest(connection: EasyDiffusionConnection): EasyDiffusionRequest {
    require(base64MaskImage.isBlank()) { "Easy Diffusion inpainting is unsupported" }
    return EasyDiffusionRequest(
        url = connection.url,
        sessionId = Uuid.random().toString(),
        model = connection.model,
        prompt = prompt,
        negativePrompt = negativePrompt,
        width = width,
        height = height,
        steps = samplingSteps,
        cfgScale = cfgScale,
        seed = easyDiffusionSeed(seed),
        batchCount = batchCount,
        inputImage = base64Image.takeIf(String::isNotBlank)
            ?.let { if (it.startsWith("data:image/")) it else "data:image/png;base64,$it" }
            ?: error("Choose an input image"),
        strength = denoisingStrength,
        selectionRevision = connection.selectionRevision,
    )
}

/** Resolves the normal blank/-1 random sentinel and rejects out-of-range manual values.
 * @param value User-entered decimal seed or the normal random sentinel.
 * @author Dmitriy Moroz */
private fun easyDiffusionSeed(value: String): Long = when (value) {
    "", "-1" -> Random.nextLong(4294967296L)
    else -> requireNotNull(
        value.toLongOrNull()
            ?.takeIf { it in 0L..4294967295L }) { "Seed must be between 0 and 4294967295" }
}
