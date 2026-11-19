package com.shifthackz.aisdv1.domain.usecase.debug

import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository

/**
 * Adds a deliberately invalid gallery image for the developer diagnostics action.
 *
 * @param repository Persists the diagnostic gallery entry.
 * @param timeProvider Supplies its creation timestamp and invalid image content.
 *
 * @author Dmitriy Moroz
 */
internal class DebugInsertBadBase64UseCaseImpl(
    private val repository: GenerationResultRepository,
    private val timeProvider: TimeProvider,
) : DebugInsertBadBase64UseCase {

    /**
     * Inserts one diagnostic gallery entry with intentionally invalid image bytes.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() {
        repository.insert(stubBadBase64Generation(timeProvider.currentTimeMillis()))
    }

    companion object {
        /**
         * Builds an invalid-image entry with otherwise usable generation metadata.
         *
         * @param now Creation timestamp also used as deliberately invalid Base64 content.
         * @author Dmitriy Moroz
         */
        private fun stubBadBase64Generation(now: Long) = AiGenerationResult(
            0L,
            image = now.toString(),
            inputImage = "",
            createdAt = now,
            type = AiGenerationResult.Type.TEXT_TO_IMAGE,
            prompt = "",
            negativePrompt = "",
            width = 512,
            height = 512,
            samplingSteps = 30,
            cfgScale = 0f,
            restoreFaces = false,
            sampler = "",
            seed = "",
            subSeed = "",
            subSeedStrength = 0f,
            denoisingStrength = 0f,
            hidden = false,
        )
    }
}
