package com.shifthackz.aisdv1.feature.civitai

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository
import com.shifthackz.aisdv1.domain.usecase.civitai.AutoUploadToCivitaiUseCase
import com.shifthackz.aisdv1.domain.usecase.civitai.UploadToCivitaiUseCase
import kotlinx.coroutines.CancellationException

/**
 * Applies explicit automatic-sharing consent after generation
 * publication failure cannot discard an output.
 *
 * @param repository Reads saved consent and account authorization.
 * @param gallery Saves a completed output when gallery autosave was disabled.
 * @param upload Shared single-flight publication operation.
 * @author Dmitriy Moroz
 */
internal class AutoUploadToCivitaiUseCaseImpl(
    private val repository: CivitaiRepository,
    private val gallery: GenerationResultRepository,
    private val upload: UploadToCivitaiUseCase,
) : AutoUploadToCivitaiUseCase {
    /**
     * Saves and publishes an output when explicitly enabled, preserving the generated result on upload failure.
     *
     * @param result Newly completed provider output and its original provenance.
     * @return The output with its durable gallery identity if it was saved for publication.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(result: AiGenerationResult): AiGenerationResult {
        var saved = result
        try {
            if (repository.isAutoUploadEnabled() && repository.account() != null) {
                if (saved.id <= 0) saved = saved.copy(id = gallery.insert(saved))
                upload(saved.id)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // Optional-publication recovery belongs here, after preserving the completed image.
        }
        return saved
    }
}
