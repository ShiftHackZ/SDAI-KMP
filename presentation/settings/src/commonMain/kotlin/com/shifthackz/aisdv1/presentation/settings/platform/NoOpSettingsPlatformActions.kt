package com.shifthackz.aisdv1.presentation.settings.platform

/**
 * Common fallback used in previews/tests and unsupported targets.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpSettingsPlatformActions : SettingsPlatformActions {

    override val requiresStoragePermissionForMediaStore = false

    override val supportsBackgroundGeneration = false

    override val supportsConnectionTimeout = false

    override val isNnapiAvailable = false

    override val backgroundGenerationWarningKey = "settings_item_background_generation_warning"

    override suspend fun requestStoragePermission(): Boolean = true

    override suspend fun requestNotificationPermission(): Boolean = true

    override suspend fun getAppCacheBytes(): Long = 0L

    override suspend fun clearAppCache() = Unit

    override suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long = 0L

    override fun openUrl(url: String) = Unit

    override fun shareLogFile() = Unit

    override fun showDeveloperModeUnlocked() = Unit

    override fun openAppSettings() = Unit
}
