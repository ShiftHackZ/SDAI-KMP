package com.shifthackz.aisdv1.domain.usecase.caching

/**
 * Caches remote metadata needed during connection and application startup.
 * LoRA discovery is deferred until the extras picker is opened, because large
 * libraries can take longer to scan than the connection deadline permits.
 *
 * @author Dmitriy Moroz
 */
interface DataPreLoaderUseCase {
    /**
     * Fetches connection metadata and propagates failures or cancellation to the caller.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke()
}

/**
 * Provides the `NoOpDataPreLoaderUseCase` singleton used by the SDAI domain layer.
 *
 * @author Dmitriy Moroz
 */
object NoOpDataPreLoaderUseCase : DataPreLoaderUseCase {
    /**
     * Executes the `invoke` step in the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() = Unit
}
