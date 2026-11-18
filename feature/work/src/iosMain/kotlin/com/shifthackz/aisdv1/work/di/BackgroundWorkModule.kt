package com.shifthackz.aisdv1.work.di

import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.work.BackgroundTaskManagerImpl
import com.shifthackz.aisdv1.work.BackgroundWorkObserverImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import com.shifthackz.aisdv1.domain.feature.work.ModelDownloadManager
import com.shifthackz.aisdv1.work.ModelDownloadManagerImpl

/**
 * Binds platform task scheduling, progress observation and model download ownership.
 *
 * @author Dmitriy Moroz
 */
actual val backgroundWorkModule = module {
    single<ModelDownloadManager> {
        ModelDownloadManagerImpl(
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.Main)
        )
    }
    singleOf(::BackgroundWorkObserverImpl) bind BackgroundWorkObserver::class
    singleOf(::BackgroundTaskManagerImpl) bind BackgroundTaskManager::class
}
