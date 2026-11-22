package com.shifthackz.aisdv1.feature.civitai

import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository
import com.shifthackz.aisdv1.domain.usecase.civitai.UploadToCivitaiUseCase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serializes manual and automatic publication and checks durable receipts before sending bytes.
 *
 * @param civitai Strict account/publication persistence boundary.
 * @param gallery Loads the original output and recorded generation provenance.
 * @param description Builds visible SDAI attribution without current-device substitutions.
 * @author Dmitriy Moroz
 */
internal class UploadToCivitaiUseCaseImpl(
    private val civitai: CivitaiRepository,
    private val gallery: GenerationResultRepository,
    private val description: CivitaiDescription,
) : UploadToCivitaiUseCase {
    private val mutex = Mutex()

    /**
     * Publishes one saved output or returns its existing public post URL.
     *
     * @param imageId Saved gallery identity
     * transient images must be persisted first.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(imageId: Long): String = mutex.withLock {
        require(imageId > 0)
        checkNotNull(civitai.account()) { "CIVITAI_SIGN_IN_REQUIRED" }
        civitai.receipt(imageId)?.let {
            if (it.status == CivitaiUploadStatus.PUBLISHED) return@withLock checkNotNull(it.url)
            check(it.status != CivitaiUploadStatus.UNCERTAIN) { "CIVITAI_PUBLICATION_UNCERTAIN" }
        }
        val result = gallery.getById(imageId)
        result.civitaiUrl ?: checkNotNull(civitai.publish(result, description(result)).url)
    }
}
