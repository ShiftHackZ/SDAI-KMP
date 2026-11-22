package com.shifthackz.aisdv1.feature.civitai

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Formats public attribution without credentials, server addresses or input images.
 *
 * @author Dmitriy Moroz
 */
internal class CivitaiDescriptionImpl : CivitaiDescription {
    /**
     * Builds a visible attribution and recorded parameters, omitting unavailable provenance.
     *
     * @param result Saved output metadata from the originating generation.
     * @author Dmitriy Moroz
     */
    override fun invoke(result: AiGenerationResult): String = with(result) {
        val metadata = buildString {
            providerKey?.let { appendLine("Generation provider: $it") }
            if (modelName.isNotBlank()) appendLine("Model: $modelName")
            deviceName?.let { appendLine("Phone / device: $it") }
            osVersion?.let { appendLine("Platform / OS: $it") }
            acceleration?.let { appendLine("Acceleration: $it") }
            generationDurationMillis?.let { appendLine("Generation time: $it ms") }
            appendLine("Type: ${type.key}")
            appendLine("Prompt: $prompt")
            appendLine("Negative prompt: $negativePrompt")
            appendLine("Size: ${width}x$height")
            appendLine("Steps: $samplingSteps; CFG scale: $cfgScale; Sampler: $sampler")
            appendLine("Seed: $seed; Variation seed: $subSeed; Variation strength: $subSeedStrength")
            appendLine("Denoising strength: $denoisingStrength; Restore faces: $restoreFaces")
        }.trim().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        "<p>Created with <strong>SDAI</strong> — AI image generation for Android and iOS. " +
                "<a href=\"https://sdai.moroz.cc\">Get SDAI</a></p><pre>$metadata</pre>"
    }
}
