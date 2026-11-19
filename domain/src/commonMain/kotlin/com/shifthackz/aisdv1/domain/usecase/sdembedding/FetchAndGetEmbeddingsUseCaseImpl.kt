package com.shifthackz.aisdv1.domain.usecase.sdembedding

import com.shifthackz.aisdv1.domain.entity.Embedding
import com.shifthackz.aisdv1.domain.repository.EmbeddingsRepository

/**
 * Refreshes and reads the configured server embedding catalog.
 *
 * @param repository Refreshes embedding descriptors and reads the resulting catalog.
 *
 * @author Dmitriy Moroz
 */
internal class FetchAndGetEmbeddingsUseCaseImpl(
    private val repository: EmbeddingsRepository,
) : FetchAndGetEmbeddingsUseCase {

    /**
     * Returns the embedding catalog after its repository refresh.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(): List<Embedding> = repository.fetchAndGetEmbeddings()
}
