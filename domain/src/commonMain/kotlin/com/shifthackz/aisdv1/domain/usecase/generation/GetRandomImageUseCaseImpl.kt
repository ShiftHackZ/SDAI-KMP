package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.repository.RandomImageRepository

/**
 * Loads a random reference image for the generation input.
 *
 * @param randomImageRepository Fetches and stores the random reference image.
 *
 * @author Dmitriy Moroz
 */
internal class GetRandomImageUseCaseImpl(
    private val randomImageRepository: RandomImageRepository,
) : GetRandomImageUseCase {

    /**
     * Returns the reference image supplied by the repository.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() = randomImageRepository.fetchAndGet()
}
