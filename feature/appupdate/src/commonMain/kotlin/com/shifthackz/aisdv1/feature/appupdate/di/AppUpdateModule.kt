package com.shifthackz.aisdv1.feature.appupdate.di

import com.shifthackz.aisdv1.feature.appupdate.CheckAppUpdateUseCaseImpl
import com.shifthackz.aisdv1.feature.appupdate.domain.CheckAppUpdateUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Distribution discovery and its optional launch policy.
 *
 * @author Dmitriy Moroz
 */
val appUpdateModule = module {
    includes(platformAppUpdateModule())
    factory<CheckAppUpdateUseCase> { CheckAppUpdateUseCaseImpl(get()) }
}

/**
 * Installs a distribution-specific gateway without leaking native types into its contract.
 *
 * @author Dmitriy Moroz
 */
internal expect fun platformAppUpdateModule(): Module
