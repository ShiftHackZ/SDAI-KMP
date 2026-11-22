package com.shifthackz.aisdv1.presentation.backup.service

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.gateway.backup.BackupChangesGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.presentation.components.service.storage.StorageUsageObserver

/**
 * Keeps retained UI snapshots consistent with committed portable data without feature-to-presentation dependencies.
 *
 * @param preferences Current checked settings and their shared invalidation flow.
 * @param storage Shared storage usage revision observed by Settings and storage screens.
 * @author Dmitriy Moroz
 */
internal class BackupChangesGatewayImpl(
    private val preferences: PreferenceManager,
    private val storage: StorageUsageObserver
) : BackupChangesGateway {
    override suspend fun notifyChanged(categories: Set<BackupCategory>) {
        if (BackupCategory.SETTINGS in categories) Localization.setLanguageCode(preferences.languageCode.takeIf { it.isNotBlank() })
        if (categories.isNotEmpty()) preferences.refresh()
        if (BackupCategory.GALLERY in categories) storage.notifyChanged()
    }
}
