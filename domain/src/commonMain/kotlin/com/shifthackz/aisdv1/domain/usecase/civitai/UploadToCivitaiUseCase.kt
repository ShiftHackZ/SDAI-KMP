package com.shifthackz.aisdv1.domain.usecase.civitai

/**
 * Publishes one saved output or returns its existing public post URL.
 *
 * @param imageId Saved gallery identity
 * transient images must be persisted first.
 *
 * @author Dmitriy Moroz
 */
fun interface UploadToCivitaiUseCase {
    /**
     * Publishes one saved output or returns its existing public post URL.
     *
     * @param imageId Saved gallery identity
     * transient images must be persisted first.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(imageId: Long): String
}
