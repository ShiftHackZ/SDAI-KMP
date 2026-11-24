package com.shifthackz.aisdv1.domain.usecase.faq

import com.shifthackz.aisdv1.domain.entity.FaqContent
import com.shifthackz.aisdv1.domain.repository.FaqRepository

/**
 * Keeps usable online content available even if writing the offline copy fails.
 *
 * @param repository Source for downloads and persistent FAQ snapshots.
 * @author Dmitriy Moroz
 */
internal class FetchOrGetFaqUseCaseImpl(
    private val repository: FaqRepository,
) : FetchOrGetFaqUseCase {
    override suspend fun invoke(): Result<FaqContent> {
        val categories = repository.fetch().getOrElse { failure ->
            val cached = repository.readCache().getOrElse { return Result.failure(it) }
            if (cached.isEmpty()) return Result.failure(failure)
            return Result.success(FaqContent(cached, isCached = true))
        }
        // The fresh answers remain available if saving the offline snapshot fails.
        repository.writeCache(categories)
        return Result.success(FaqContent(categories, isCached = false))
    }
}
