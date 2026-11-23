package com.shifthackz.aisdv1.presentation.logger

/**
 * Provides side-effect-free clipboard/sharing operations when no native adapter is available.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpLoggerPlatformActions : LoggerPlatformActions {

    override suspend fun copyLogs(text: String) = Unit

    override suspend fun shareLogs(text: String) = Unit
}
