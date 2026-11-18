package com.shifthackz.aisdv1.presentation.settings.di

import com.shifthackz.aisdv1.presentation.settings.platform.NoOpSettingsPlatformActions
import com.shifthackz.aisdv1.presentation.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.settings.DebugMenuAccessor
import com.shifthackz.aisdv1.presentation.settings.DebugMenuAccessorImpl
import org.koin.dsl.module


/**
 * Retains one developer-unlock counter and supplies default settings actions until a native adapter replaces them.
 *
 * @author Dmitriy Moroz
 */
val settingsCoreModule = module {
    single<DebugMenuAccessor> { DebugMenuAccessorImpl(get()) }
    single<SettingsPlatformActions> { NoOpSettingsPlatformActions }
}
