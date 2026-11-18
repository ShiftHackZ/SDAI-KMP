package com.shifthackz.aisdv1.presentation.debug

/**
 * Platform debug operations; callers present the returned failures and hide unsupported sections.
 *
 * @author Dmitriy Moroz
 */
interface DebugMenuPlatformActions {
    /**
     * Whether the debug menu exposes background-work controls on this platform.
     *
     * @author Dmitriy Moroz
     */
    val showWorkManagerSection: Boolean

    /**
     * Whether this platform supports the local-inference debug controls.
     *
     * @author Dmitriy Moroz
     */
    val showLocalDiffusionSection: Boolean

    /**
     * Deletes the app-owned log cache, reporting filesystem failures in the result.
     *
     * @author Dmitriy Moroz
     */
    suspend fun clearLogs(): Result<Unit>

    /**
     * Resubmits the last captured text-to-image background task and reports scheduling failures.
     *
     * @author Dmitriy Moroz
     */
    fun retryLastTextToImageTask(): Result<Unit>

    /**
     * Resubmits the last captured image-to-image background task and reports scheduling failures.
     *
     * @author Dmitriy Moroz
     */
    fun retryLastImageToImageTask(): Result<Unit>

    /**
     * Cancels all application background-generation work and reports scheduling failures.
     *
     * @author Dmitriy Moroz
     */
    fun cancelAllWork(): Result<Unit>
}
