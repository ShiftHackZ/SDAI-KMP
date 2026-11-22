package com.shifthackz.aisdv1.feature.backup.di

import com.shifthackz.aisdv1.feature.backup.io.BackupFileStore
import com.shifthackz.aisdv1.feature.backup.io.AndroidBackupFileStore
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

/**
 * Binds native document transport while retaining the shared backup API.
 *
 * @author Dmitriy Moroz
 */
internal actual val backupPlatformModule: Module =
    module { single<BackupFileStore> { AndroidBackupFileStore(androidContext()) } }
