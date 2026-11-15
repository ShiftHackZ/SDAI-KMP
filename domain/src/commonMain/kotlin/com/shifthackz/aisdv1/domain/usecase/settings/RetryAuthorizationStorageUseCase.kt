package com.shifthackz.aisdv1.domain.usecase.settings

/**
 * Explicitly retries an interrupted local configuration/credential transition without network replay.
 *
 * @author Dmitriy Moroz
 */
interface RetryAuthorizationStorageUseCase {
    /**
     * Completes checked persistence/recovery before allowing protected credential reads.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke()
}
