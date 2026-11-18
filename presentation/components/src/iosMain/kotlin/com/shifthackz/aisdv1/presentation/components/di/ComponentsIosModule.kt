package com.shifthackz.aisdv1.presentation.components.di

import com.shifthackz.aisdv1.presentation.components.core.GenerationPlatformServices
import com.shifthackz.aisdv1.presentation.components.core.IosGenerationPlatformServices
import com.shifthackz.aisdv1.presentation.components.platform.StorageUsageFileSystem
import com.shifthackz.aisdv1.presentation.components.platform.IosStorageUsageFileSystem
import org.koin.dsl.module

/**
 * Installs the iOS generation lifecycle adapter and shared app-private storage accounting before rendering.
 *
 * @author Dmitriy Moroz
 */
val componentsIosModule = module {
    single<GenerationPlatformServices> { IosGenerationPlatformServices }
    single<StorageUsageFileSystem> { IosStorageUsageFileSystem() }
}
