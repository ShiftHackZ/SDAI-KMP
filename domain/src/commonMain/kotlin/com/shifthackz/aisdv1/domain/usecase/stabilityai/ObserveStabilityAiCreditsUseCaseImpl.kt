package com.shifthackz.aisdv1.domain.usecase.stabilityai

import com.shifthackz.aisdv1.domain.repository.StabilityAiCreditsRepository
import kotlinx.coroutines.flow.catch

/**
 * Observes refreshed Stability AI credits, emitting zero when the credit stream fails.
 *
 * @param repository Refreshes and observes the account credit balance.
 *
 * @author Dmitriy Moroz
 */
internal class ObserveStabilityAiCreditsUseCaseImpl(
    private val repository: StabilityAiCreditsRepository,
) : ObserveStabilityAiCreditsUseCase {

    /**
     * Emits account credits, or zero if refreshing or observing fails.
     *
     * @author Dmitriy Moroz
     */
    override fun invoke() = repository
        .fetchAndObserve()
        .catch { emit(0f) }
}
