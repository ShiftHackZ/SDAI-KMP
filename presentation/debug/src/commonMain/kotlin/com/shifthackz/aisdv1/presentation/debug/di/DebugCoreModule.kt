package com.shifthackz.aisdv1.presentation.debug.di

import com.shifthackz.aisdv1.presentation.debug.UiDiagnosticReportActions
import com.shifthackz.aisdv1.presentation.debug.NoOpUiDiagnosticReportActions
import com.shifthackz.aisdv1.presentation.debug.DebugMenuPlatformActions
import com.shifthackz.aisdv1.presentation.debug.NoOpDebugMenuPlatformActions
import org.koin.dsl.module

/**
 * Supplies side-effect-free singleton diagnostic/debug adapters until platform bindings replace them.
 *
 * @author Dmitriy Moroz
 */
val debugCoreModule = module {
    single<UiDiagnosticReportActions> { NoOpUiDiagnosticReportActions }
    single<DebugMenuPlatformActions> { NoOpDebugMenuPlatformActions }
}
