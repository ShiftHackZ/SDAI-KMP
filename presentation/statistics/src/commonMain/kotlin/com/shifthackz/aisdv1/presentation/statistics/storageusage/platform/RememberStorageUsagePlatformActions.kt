package com.shifthackz.aisdv1.presentation.statistics.storageusage.platform

import androidx.compose.runtime.Composable

/**
 * Remembers the platform-specific storage bridge used by the storage usage screen.
 *
 * @author Dmitriy Moroz
 */
@Composable
expect fun rememberStorageUsagePlatformActions(): StorageUsagePlatformActions
