package com.shifthackz.aisdv1.work.di

import com.shifthackz.aisdv1.work.IosWorkExecution
import org.koin.core.Koin
import org.koin.dsl.module

/**
 * Installs the native execution bridge before any background manager is resolved.
 *
 * @param koin Application dependency graph created by the native composition root.
 * @param execution Native host implementation retaining per-request execution grants.
 * @author Dmitriy Moroz
 */
fun installIosWorkExecution(koin: Koin, execution: IosWorkExecution) {
    koin.loadModules(listOf(module { single<IosWorkExecution> { execution } }))
}
