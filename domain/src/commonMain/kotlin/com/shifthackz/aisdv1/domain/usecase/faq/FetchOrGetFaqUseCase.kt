package com.shifthackz.aisdv1.domain.usecase.faq

import com.shifthackz.aisdv1.domain.entity.FaqContent

/**
 * Resolves current FAQ content for a screen visit or retry.
 *
 * @author Dmitriy Moroz
 */
interface FetchOrGetFaqUseCase {
    /**
     * Fetches the latest content first and falls back to the saved copy on failure.
     * If neither source can supply content, returns a failure. Cancellation propagates.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): Result<FaqContent>
}
