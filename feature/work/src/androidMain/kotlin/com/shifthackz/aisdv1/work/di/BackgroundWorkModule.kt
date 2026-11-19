package com.shifthackz.aisdv1.work.di

import androidx.work.WorkManager
import com.shifthackz.aisdv1.work.GenerationWorkPayloadCleanup
import com.shifthackz.aisdv1.work.GenerationWorkPayloadCleanupImpl
import com.shifthackz.aisdv1.work.GenerationWorkPayloadStore
import com.shifthackz.aisdv1.work.FileGenerationWorkPayloadStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import androidx.work.WorkerFactory
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.work.BackgroundTaskManagerImpl
import com.shifthackz.aisdv1.work.BackgroundWorkObserverImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import com.shifthackz.aisdv1.domain.feature.work.ModelDownloadManager
import com.shifthackz.aisdv1.work.ModelDownloadManagerImpl

/**
 * Binds platform task scheduling, progress observation and model download ownership.
 *
 * @author Dmitriy Moroz
 */
actual val backgroundWorkModule = module {
    singleOf(::ModelDownloadManagerImpl) bind ModelDownloadManager::class
    factory {
        WorkManagerProvider { WorkManager.getInstance(androidApplication()) }
    }

    factoryOf(::SdaiWorkerFactory) binds arrayOf(SdaiWorkerFactory::class, WorkerFactory::class)
    single<BackgroundWorkObserver> {
        BackgroundWorkObserverImpl(
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
    }
    singleOf(::GenerationWorkPayloadCleanupImpl) bind GenerationWorkPayloadCleanup::class
    singleOf(::FileGenerationWorkPayloadStore) bind GenerationWorkPayloadStore::class
    single<BackgroundTaskManager> {
        BackgroundTaskManagerImpl(
            get(),
            get(),
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
    }
}
