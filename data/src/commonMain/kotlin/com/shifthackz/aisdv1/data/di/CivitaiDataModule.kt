package com.shifthackz.aisdv1.data.di

import com.shifthackz.aisdv1.domain.datasource.CivitaiRemoteDataSource
import com.shifthackz.aisdv1.data.remote.CivitaiRemoteDataSourceImpl
import com.shifthackz.aisdv1.data.repository.CivitaiRepositoryImpl
import com.shifthackz.aisdv1.data.preference.createKeyValueStore
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.domain.repository.CivitaiUploadHistoryRepository
import com.shifthackz.aisdv1.data.repository.CivitaiUploadHistoryRepositoryImpl
import com.shifthackz.aisdv1.domain.datasource.CivitaiUploadLocalDataSource
import com.shifthackz.aisdv1.data.local.CivitaiUploadLocalDataSourceImpl
import org.koin.dsl.module

/**
 * Isolated Civitai data owners
 * never inherit generation-provider credentials or body logging.
 *
 * @author Dmitriy Moroz
 */
val civitaiDataModule = module {
    includes(civitaiDataPlatformModule)
    single<CivitaiRemoteDataSource> { CivitaiRemoteDataSourceImpl(get()) }
    single<CivitaiUploadLocalDataSource> {
        CivitaiUploadLocalDataSourceImpl(
            get(),
            createKeyValueStore("civitai")
        )
    }
    single<CivitaiUploadHistoryRepository> { CivitaiUploadHistoryRepositoryImpl(get()) }
    single<CivitaiRepository> {
        CivitaiRepositoryImpl(
            get(),
            createKeyValueStore("civitai"),
            get(),
            get(),
            get(),
            get()
        )
    }
}
