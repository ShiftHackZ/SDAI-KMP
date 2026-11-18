package com.shifthackz.aisdv1.presentation.debug

/**
 * Hides unsupported platform sections and performs debug operations without side effects.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpDebugMenuPlatformActions : DebugMenuPlatformActions {

    override val showWorkManagerSection = false

    override val showLocalDiffusionSection = false

    override suspend fun clearLogs(): Result<Unit> = Result.success(Unit)

    override fun retryLastTextToImageTask(): Result<Unit> = Result.success(Unit)

    override fun retryLastImageToImageTask(): Result<Unit> = Result.success(Unit)

    override fun cancelAllWork(): Result<Unit> = Result.success(Unit)
}
