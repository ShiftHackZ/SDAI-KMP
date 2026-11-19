package com.shifthackz.aisdv1.domain.usecase.sdlora

import com.shifthackz.aisdv1.domain.repository.LorasRepository

/**
 * Refreshes and reads the configured server LoRA catalog.
 *
 * @param lorasRepository Refreshes LoRA descriptors and reads the resulting catalog.
 *
 * @author Dmitriy Moroz
 */
internal class FetchAndGetLorasUseCaseImpl(
    private val lorasRepository: LorasRepository,
) : FetchAndGetLorasUseCase {

    /**
     * Returns the LoRA catalog after its repository refresh.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() = lorasRepository.fetchAndGetLoras()
}
