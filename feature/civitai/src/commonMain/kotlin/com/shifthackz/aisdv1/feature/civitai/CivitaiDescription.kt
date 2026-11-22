package com.shifthackz.aisdv1.feature.civitai

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Public description builder shared by manual and automatic uploads.
 *
 * @author Dmitriy Moroz
 */
internal interface CivitaiDescription {
    /**
     * Builds a visible attribution and recorded parameters, omitting unavailable provenance.
     *
     * @param result Saved output metadata from the originating generation.
     * @author Dmitriy Moroz
     */
    operator fun invoke(result: AiGenerationResult): String
}
