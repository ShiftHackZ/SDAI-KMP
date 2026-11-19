package com.shifthackz.aisdv1.domain.usecase.donate

import com.shifthackz.aisdv1.domain.entity.Supporter
import com.shifthackz.aisdv1.domain.repository.SupportersRepository

/**
 * Refreshes the supporter list through its repository.
 *
 * @param repository Loads the current supporter list and its stored representation.
 *
 * @author Dmitriy Moroz
 */
internal class FetchSupportersUseCaseImpl(
    private val repository: SupportersRepository,
) : FetchSupportersUseCase {

    /**
     * Returns supporters after the repository refresh completes.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(): List<Supporter> = repository.fetchAndGetSupporters()
}
