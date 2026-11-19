package com.shifthackz.aisdv1.data.di

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.data.gateway.mediastore.IosMediaStoreGateway
import com.shifthackz.aisdv1.data.local.DownloadableModelFileStore
import com.shifthackz.aisdv1.data.local.IosDownloadableModelFileStore
import com.shifthackz.aisdv1.data.local.IosMnnModelFileStore
import com.shifthackz.aisdv1.data.repository.MnnGenerationRepositoryImpl
import com.shifthackz.aisdv1.data.validator.MnnBundleValidatorImpl
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import com.shifthackz.aisdv1.domain.gateway.MnnModelStore
import com.shifthackz.aisdv1.data.remote.DownloadableModelFileDownloader
import com.shifthackz.aisdv1.data.remote.IosDownloadableModelFileDownloader
import com.shifthackz.aisdv1.data.repository.IosWakeLockRepository
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.repository.WakeLockRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import com.shifthackz.aisdv1.data.performance.IosPerformanceGateway
import com.shifthackz.aisdv1.domain.gateway.PerformanceGateway
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSHomeDirectory

/**
 * Replaces unsupported defaults with iOS storage, export and injected MNN generation.
 *
 * @author Dmitriy Moroz
 */
private val iosDataOverridesModule = module {
    single<PerformanceGateway> { IosPerformanceGateway() }
    singleOf(::MnnBundleValidatorImpl) bind MnnBundleValidator::class
    singleOf(::IosMnnModelFileStore) bind MnnModelStore::class
    singleOf(::MnnGenerationRepositoryImpl) bind MnnGenerationRepository::class
    single<FileProviderDescriptor> {
        IosFileProviderDescriptor()
    }
    single<MediaStoreGateway> {
        IosMediaStoreGateway()
    }
    single<WakeLockRepository> {
        IosWakeLockRepository()
    }
    factoryOf(::IosDownloadableModelFileDownloader) bind DownloadableModelFileDownloader::class
    factoryOf(::IosDownloadableModelFileStore) bind DownloadableModelFileStore::class
}

/**
 * Installs iOS repositories after the common data graph.
 *
 * @author Dmitriy Moroz
 */
actual val dataModule = arrayOf(iosDataOverridesModule)

private class IosFileProviderDescriptor : FileProviderDescriptor {

    private val homePath = NSHomeDirectory()
    private val cachePath = "$homePath/Library/Caches"

    override val providerPath: String =
        NSBundle.mainBundle.bundleIdentifier ?: "com.shifthackz.aisdv1.app"

    override val imagesCacheDirPath: String = "$cachePath/images"

    override val logsCacheDirPath: String = "$cachePath/logs"

    override val localModelDirPath: String = "$homePath/Library/Application Support/model"

    override val workCacheDirPath: String = "$cachePath/work"
}
