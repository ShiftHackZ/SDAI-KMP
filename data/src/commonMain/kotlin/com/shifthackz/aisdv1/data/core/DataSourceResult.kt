package com.shifthackz.aisdv1.data.core

import kotlinx.coroutines.CancellationException

/**
 * Captures an operation's exception for the caller while preserving coroutine cancellation and fatal errors.
 *
 * @param operation Data source operation whose result is reported.
 * @author Dmitriy Moroz
 */
internal inline fun <T> dataSourceResultOf(operation: () -> T): Result<T> =
    runCatching(operation).onFailure { failure ->
        if (failure is CancellationException || failure !is Exception) throw failure
    }
