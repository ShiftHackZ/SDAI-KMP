package com.shifthackz.aisdv1.presentation.configuration.setup.platform

/**
 * Runs downloads directly on platforms that do not require a wake lock.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpServerSetupDownloadGuard : ServerSetupDownloadGuard {
    override suspend fun <T> withDownload(block: suspend () -> T): T = block()
}
