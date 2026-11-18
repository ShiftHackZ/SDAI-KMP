package com.shifthackz.aisdv1.presentation.debug

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.log.FileLoggingTree
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager

/**
 * Adapts debug operations to Android log storage and the shared background-work scheduler.
 *
 * @param fileProviderDescriptor Locates the app-owned log cache cleared by the debug menu.
 * @param backgroundTaskManager Resubmits captured generation tasks or cancels scheduled work.
 *
 * @author Dmitriy Moroz
 */
class AndroidDebugMenuPlatformActions(
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val backgroundTaskManager: BackgroundTaskManager,
) : DebugMenuPlatformActions {

    override val showWorkManagerSection = true

    override val showLocalDiffusionSection = true

    /**
     * Clears application log files; the result contains filesystem failures.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun clearLogs(): Result<Unit> = runCatching {
        FileLoggingTree.clearLog(fileProviderDescriptor)
    }

    /**
     * Delegates resubmission of the last saved text-to-image task to the background scheduler.
     *
     * @author Dmitriy Moroz
     */
    override fun retryLastTextToImageTask(): Result<Unit> =
        backgroundTaskManager.retryLastTextToImageTask()

    /**
     * Delegates resubmission of the last saved image-to-image task to the background scheduler.
     *
     * @author Dmitriy Moroz
     */
    override fun retryLastImageToImageTask(): Result<Unit> =
        backgroundTaskManager.retryLastImageToImageTask()

    /**
     * Requests cancellation of all application background-generation tasks.
     *
     * @author Dmitriy Moroz
     */
    override fun cancelAllWork(): Result<Unit> =
        backgroundTaskManager.cancelAll()
}
