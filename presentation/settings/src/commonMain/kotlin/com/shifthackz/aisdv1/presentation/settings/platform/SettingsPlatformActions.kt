package com.shifthackz.aisdv1.presentation.settings.platform

import com.shifthackz.aisdv1.presentation.components.model.StorageUsageByteMapper

/**
 * Platform bridge for Settings actions that cannot be implemented in common code.
 *
 * Implementations request Android/iOS permissions, open external URLs or app settings,
 * and expose platform capability flags used to hide unsupported Settings rows.
 *
 * @author Dmitriy Moroz
 */
interface SettingsPlatformActions : StorageUsageByteMapper {
    /**
     * True when saving generated images through MediaStore still needs a runtime storage permission.
     *
     * @author Dmitriy Moroz
     */
    val requiresStoragePermissionForMediaStore: Boolean

    /**
     * True when this target can continue generation work after the app leaves foreground.
     *
     * @author Dmitriy Moroz
     */
    val supportsBackgroundGeneration: Boolean

    /**
     * True when this platform enforces application setup and metadata request deadlines.
     *
     * @author Dmitriy Moroz
     */
    val supportsConnectionTimeout: Boolean

    /**
     * Whether Settings can offer NNAPI: Android 8.1 (API 27) and newer only.
     * Availability does not guarantee that the device or model supports hardware acceleration.
     *
     * @author Dmitriy Moroz
     */
    val isNnapiAvailable: Boolean

    /**
     * Localization key for the warning shown under the background generation toggle.
     *
     * @author Dmitriy Moroz
     */
    val backgroundGenerationWarningKey: String

    /**
     * Requests storage/media permission when the platform requires it for gallery saves.
     *
     * @author Dmitriy Moroz
     */
    suspend fun requestStoragePermission(): Boolean

    /**
     * Requests notification permission when the platform requires it for background generation.
     *
     * @author Dmitriy Moroz
     */
    suspend fun requestNotificationPermission(): Boolean

    /**
     * Returns current app cache byte size for the Settings summary row.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAppCacheBytes(): Long

    /**
     * Clears app cache files after a caller has shown destructive confirmation.
     *
     * @author Dmitriy Moroz
     */
    suspend fun clearAppCache()


    /**
     * Returns total bytes occupied by downloaded model directories for [modelIds].
     *
     * @param modelIds Local model identifiers whose downloaded directories should be measured.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long

    /**
     * Opens an external URL through the platform.
     *
     * @param url Absolute URL or platform-supported URI to open.
     *
     * @author Dmitriy Moroz
     */
    fun openUrl(url: String)

    /**
     * Opens the problem-report channel with diagnostic context prepared by the platform.
     *
     * @author Dmitriy Moroz
     */
    fun shareLogFile()

    /**
     * Shows platform feedback after developer mode is unlocked.
     *
     * @author Dmitriy Moroz
     */
    fun showDeveloperModeUnlocked()

    /**
     * Opens this app's platform settings page.
     *
     * @author Dmitriy Moroz
     */
    fun openAppSettings()
}
