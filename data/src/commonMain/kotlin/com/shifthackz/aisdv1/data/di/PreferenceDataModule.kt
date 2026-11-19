package com.shifthackz.aisdv1.data.di

import com.shifthackz.aisdv1.data.preference.KeyValueConfigurationStore
import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.data.preference.PreferenceManagerImpl
import com.shifthackz.aisdv1.data.preference.SessionPreferenceImpl
import com.shifthackz.aisdv1.data.preference.createKeyValueStore
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.preference.SessionPreference
import com.shifthackz.aisdv1.network.client.ConnectionTimeoutProvider
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import org.koin.core.qualifier.named
import com.shifthackz.aisdv1.data.preference.backup.BackupPreferencesGatewayImpl
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.gateway.backup.BackupExportStateStore
import com.shifthackz.aisdv1.data.preference.backup.BackupExportStateStoreImpl
import org.koin.dsl.module

/**
 * Shares one checked preference owner across provider settings, portable backup adapters and live timeout readers.
 *
 * @author Dmitriy Moroz
 */
val preferenceDataModule = module {
    single<BackupExportStateStore> { BackupExportStateStoreImpl(createKeyValueStore("aisdv1_backup_operations")) }
    single<BackupPreferencesGateway> {
        BackupPreferencesGatewayImpl(
            get(named(KEY_PREFERENCE_MANAGER)),
            get()
        )
    }
    single<KeyValueStore>(named(KEY_PREFERENCE_MANAGER)) {
        createKeyValueStore(
            KEY_PREFERENCE_MANAGER
        )
    }
    single<ConnectionTimeoutProvider> {
        val preferences = get<PreferenceManager>()
        ConnectionTimeoutProvider { preferences.connectionTimeout.seconds * 1000L }
    }
    single<GenerationTimeoutProvider> {
        val preferences = get<PreferenceManager>()
        GenerationTimeoutProvider { preferences.generationRequestTimeout.seconds * 1000L }
    }
    single<PreferenceManager> {
        PreferenceManagerImpl(
            keyValueStore = get(named(KEY_PREFERENCE_MANAGER)),
        )
    }
    single<ConfigurationStore> {
        KeyValueConfigurationStore(
            keyValueStore = get(named(KEY_PREFERENCE_MANAGER)),
        )
    }
    single<SessionPreference> {
        SessionPreferenceImpl()
    }
}

/**
 * Installed preference-suite name; preserving it retains settings across app updates.
 *
 * @author Dmitriy Moroz
 */
private const val KEY_PREFERENCE_MANAGER = "aisdv1_preference_manager"
