package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository

/**
 * Shares the platform MNN status stream with foreground and finite background UI.
 *
 * @param repository Platform generation owner.
 *
 * @author Dmitriy Moroz
 */
internal class ObserveMnnProcessStatusUseCaseImpl(
    private val repository: MnnGenerationRepository
) : ObserveMnnProcessStatusUseCase {
    override fun invoke() = repository.observeStatus()
}
