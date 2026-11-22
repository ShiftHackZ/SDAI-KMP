package com.shifthackz.aisdv1.data.di

import com.shifthackz.aisdv1.data.civitai.CivitaiCredentialStore
import com.shifthackz.aisdv1.data.civitai.SecureCivitaiCredentialStore
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

/**
 * Native encrypted Civitai credential storage binding.
 *
 * @author Dmitriy Moroz
 */
internal actual val civitaiDataPlatformModule = module {
    single<CivitaiCredentialStore> { SecureCivitaiCredentialStore(androidContext()) }
}
