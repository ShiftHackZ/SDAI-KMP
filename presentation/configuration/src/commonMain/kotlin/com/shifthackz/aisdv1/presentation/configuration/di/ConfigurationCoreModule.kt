package com.shifthackz.aisdv1.presentation.configuration.di

import com.shifthackz.aisdv1.presentation.configuration.setup.platform.NoOpServerSetupDownloadGuard
import com.shifthackz.aisdv1.presentation.configuration.setup.platform.ServerSetupDownloadGuard
import org.koin.dsl.module

/**
 * Supplies the no-op download guard for hosts without Android-specific download prerequisites.
 *
 * @author Dmitriy Moroz
 */
val configurationCoreModule = module {
    single<ServerSetupDownloadGuard> { NoOpServerSetupDownloadGuard }
}
