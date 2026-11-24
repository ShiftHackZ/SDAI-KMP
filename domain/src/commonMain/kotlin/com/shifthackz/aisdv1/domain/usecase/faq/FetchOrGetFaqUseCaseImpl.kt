package com.shifthackz.aisdv1.domain.usecase.faq

import com.shifthackz.aisdv1.domain.entity.FaqContent
import com.shifthackz.aisdv1.domain.repository.FaqRepository
import kotlinx.coroutines.CancellationException

/**
 * Keeps usable online content available even if writing the offline copy fails.
 *
 * @param repository Source for downloads and persistent FAQ snapshots.
 * @author Dmitriy Moroz
 */
internal class FetchOrGetFaqUseCaseImpl(
    private val repository: FaqRepository,
) : FetchOrGetFaqUseCase {
    override suspend fun invoke(): FaqContent {
        val categories = try {
            repository.fetch()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            val cached = repository.readCache()
            if (cached.isEmpty()) throw failure
            return FaqContent(cached, isCached = true)
        }
        try {
            repository.writeCache(categories)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // The fresh answers can still be displayed if Room fails to save them.
        }
        return FaqContent(categories, isCached = false)
    }
}
