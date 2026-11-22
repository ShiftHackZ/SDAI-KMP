package com.shifthackz.aisdv1.feature.appupdate.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Distribution-specific discovery bindings for the shared launch-check policy.
 *
 * @author Dmitriy Moroz
 */
val appUpdateModule = module {
    includes(platformAppUpdateModule())
}

/**
 * Installs a distribution-specific gateway without leaking native types into its contract.
 *
 * @author Dmitriy Moroz
 */
internal expect fun platformAppUpdateModule(): Module
