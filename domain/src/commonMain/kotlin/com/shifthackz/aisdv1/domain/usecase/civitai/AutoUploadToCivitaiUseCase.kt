package com.shifthackz.aisdv1.domain.usecase.civitai

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Optional public sharing policy applied only to newly completed generations.
 *
 * @author Dmitriy Moroz
 */
fun interface AutoUploadToCivitaiUseCase {
    /**
     * Saves and publishes an output when explicitly enabled, preserving the generated result on upload failure.
     *
     * @param result Newly completed provider output and its original provenance.
     * @return The output with its durable gallery identity if it was saved for publication.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(result: AiGenerationResult): AiGenerationResult
}
