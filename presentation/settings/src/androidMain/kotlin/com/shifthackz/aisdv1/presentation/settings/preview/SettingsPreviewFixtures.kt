package com.shifthackz.aisdv1.presentation.settings.preview

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.settings.SettingsState

/**
 * Shared non-sensitive provider values used by Settings dashboard and modal previews.
 *
 * @author Dmitriy Moroz
 */
internal object SettingsPreviewFixtures {
    /**
     * Loaded Automatic1111 preferences with model choices and populated usage summaries.
     *
     * @author Dmitriy Moroz
     */
    val ready = SettingsState(
        loading = false,
        serverSource = ServerSource.AUTOMATIC1111,
        sdModels = listOf("sd15-pruned-emaonly.safetensors", "dreamshaper_8.safetensors"),
        sdModelSelected = "sd15-pruned-emaonly.safetensors",
        backgroundGenerationAvailable = true,
        monitorConnectivity = true,
        autoSaveAiResults = true,
        saveToMediaStore = true,
        storageUsageBytes = 8_400_000_000L,
        networkUsageBytes = 18_700_000_000L,
        appVersion = "1.3.0-dev.local (240)",
    )
}
