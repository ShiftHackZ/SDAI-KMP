package com.shifthackz.aisdv1.presentation.appupdate.di

import com.shifthackz.aisdv1.presentation.appupdate.AppUpdateLaunchState
import com.shifthackz.aisdv1.presentation.appupdate.AppUpdateLaunchStateImpl
import org.koin.dsl.module

/**
 * Keeps update dismissal and discovery stable for the lifetime of this application process.
 *
 * @author Dmitriy Moroz
 */
val appUpdatePresentationModule = module {
    single<AppUpdateLaunchState> { AppUpdateLaunchStateImpl(get()) }
}
