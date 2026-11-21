package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.GenerationMetadataContext
import kotlinx.coroutines.withContext

/**
 * Carries provenance through provider dispatch while preserving the original provider failure instance.
 * Returning the failure across the context boundary avoids coroutine stack-trace recovery copying it;
 * caller cancellation still propagates through withContext's normal cancellation checks.
 *
 * @param metadata Captured provider, acceleration and image start time.
 * @param block Provider operation whose result and failures must be preserved.
 * @return The provider result, rethrowing its original failure outside the context boundary.
 * @author Dmitriy Moroz
 */
internal suspend fun <T> withGenerationMetadata(
    metadata: GenerationMetadataContext,
    block: suspend () -> T,
): T = withContext(metadata) { runCatching { block() } }.getOrThrow()
