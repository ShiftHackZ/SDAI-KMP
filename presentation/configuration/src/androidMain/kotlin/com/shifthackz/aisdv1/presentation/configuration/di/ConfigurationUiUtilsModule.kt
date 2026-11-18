package com.shifthackz.aisdv1.presentation.configuration.di

import com.shifthackz.aisdv1.presentation.configuration.setup.platform.AndroidServerSetupDownloadGuard
import com.shifthackz.aisdv1.presentation.configuration.setup.platform.ServerSetupDownloadGuard
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Supplies the Android download-guard factory used to enforce platform download prerequisites.
 *
 * @author Dmitriy Moroz
 */
val configurationUiUtilsModule = module {
    factoryOf(::AndroidServerSetupDownloadGuard) bind ServerSetupDownloadGuard::class
}
