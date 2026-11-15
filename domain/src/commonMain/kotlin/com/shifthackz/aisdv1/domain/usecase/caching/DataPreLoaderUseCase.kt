package com.shifthackz.aisdv1.domain.usecase.caching

/**
 * Caches remote metadata needed during connection and application startup.
 * LoRA discovery is deferred until the extras picker is opened, because large
 * libraries can take longer to scan than the connection deadline permits.
 * Hypernetwork discovery is optional and is retried when its extras picker opens.
 *
 * @author Dmitriy Moroz
 */
interface DataPreLoaderUseCase {
    /**
     * Fetches metadata in order, continuing after a hypernetwork discovery failure.
     * Required metadata failures, cancellation and nonrecoverable errors propagate to the caller.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke()
}
