package com.shifthackz.aisdv1.presentation.configuration.setup.platform

/**
 * Runs a model download within the platform safeguard lifetime.
 *
 * Android holds a wake lock until the operation returns or fails; platforms without a safeguard
 * execute the same suspending operation directly. Cancellation and failures propagate to the caller.
 *
 * @author Dmitriy Moroz
 */
interface ServerSetupDownloadGuard {
    /**
     * Keeps the platform safeguard active while [block] executes and releases it on completion.
     *
     * @param block Download operation whose value, failure or cancellation is passed to the caller.
     *
     * @return The value produced by [block].
     *
     * @author Dmitriy Moroz
     */
    suspend fun <T> withDownload(block: suspend () -> T): T
}
