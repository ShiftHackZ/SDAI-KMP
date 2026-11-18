package com.shifthackz.aisdv1.data.di

import android.content.Context
import com.shifthackz.aisdv1.domain.gateway.MnnModelStore
import com.shifthackz.aisdv1.data.local.AndroidMnnBundleFileSystem
import com.shifthackz.aisdv1.domain.gateway.MnnBundleFileSystem
import com.shifthackz.aisdv1.data.local.AndroidMnnModelFileStore
import com.shifthackz.aisdv1.data.validator.MnnBundleValidatorImpl
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator
import com.shifthackz.aisdv1.data.repository.MnnGenerationRepositoryImpl
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import android.os.PowerManager
import com.shifthackz.aisdv1.data.export.MediaStoreImageFormatResolver
import com.shifthackz.aisdv1.data.export.MediaStoreImageFormatResolverImpl
import com.shifthackz.aisdv1.data.gateway.DatabaseClearGatewayImpl
import com.shifthackz.aisdv1.data.gateway.mediastore.MediaStoreGatewayFactory
import com.shifthackz.aisdv1.data.local.AndroidDownloadableModelFileStore
import com.shifthackz.aisdv1.data.local.DownloadableModelFileStore
import com.shifthackz.aisdv1.data.remote.AndroidDownloadableModelFileDownloader
import com.shifthackz.aisdv1.data.remote.DownloadableModelFileDownloader
import com.shifthackz.aisdv1.data.repository.LocalDiffusionGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.MediaPipeGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.repository.WakeLockRepositoryImpl
import com.shifthackz.aisdv1.domain.gateway.DatabaseClearGateway
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.MediaPipeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.WakeLockRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Supplies Android storage, download and on-device generation adapters in place of common defaults.
 *
 * @author Dmitriy Moroz
 */
private val androidDataOverridesModule = module {
    singleOf(::DatabaseClearGatewayImpl) bind DatabaseClearGateway::class
    singleOf(::MediaStoreImageFormatResolverImpl) bind MediaStoreImageFormatResolver::class
    single<MediaStoreGateway> {
        MediaStoreGatewayFactory(
            context = androidContext(),
            fileProviderDescriptor = get(),
            formatResolver = get(),
        ).invoke()
    }
    single<WakeLockRepository> {
        WakeLockRepositoryImpl {
            androidContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        }
    }

    singleOf(::AndroidMnnBundleFileSystem) bind MnnBundleFileSystem::class
    singleOf(::MnnBundleValidatorImpl) bind MnnBundleValidator::class
    singleOf(::AndroidMnnModelFileStore) bind MnnModelStore::class
    factoryOf(::MnnGenerationRepositoryImpl) bind MnnGenerationRepository::class
    factoryOf(::AndroidDownloadableModelFileDownloader) bind DownloadableModelFileDownloader::class
    factoryOf(::AndroidDownloadableModelFileStore) bind DownloadableModelFileStore::class
    factoryOf(::LocalDiffusionGenerationRepositoryImpl) bind LocalDiffusionGenerationRepository::class
    factoryOf(::MediaPipeGenerationRepositoryImpl) bind MediaPipeGenerationRepository::class
}

/**
 * Android-specific data bindings included by the application's shared composition root.
 *
 * @author Dmitriy Moroz
 */
actual val dataModule = arrayOf(androidDataOverridesModule)
