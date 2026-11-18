package com.shifthackz.aisdv1.presentation.debug.di

import com.shifthackz.aisdv1.presentation.debug.AndroidDebugMenuPlatformActions
import com.shifthackz.aisdv1.presentation.debug.DebugMenuPlatformActions
import com.shifthackz.aisdv1.presentation.debug.AndroidUiDiagnosticReportActions
import com.shifthackz.aisdv1.presentation.debug.UiDiagnosticReportActions
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Supplies Android factories for diagnostic snapshot sharing and developer log/background-work actions.
 *
 * @author Dmitriy Moroz
 */
val debugUiUtilsModule = module {
    factory<UiDiagnosticReportActions> {
        AndroidUiDiagnosticReportActions(
            androidContext(),
            get(),
            get(),
            get()
        )
    }
    factoryOf(::AndroidDebugMenuPlatformActions) bind DebugMenuPlatformActions::class
}
