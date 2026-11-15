package com.shifthackz.aisdv1.core.mvi.diagnostics

import org.koin.dsl.module

/**
 * Registers the owning module's observer implementation; release builds use the no-op. Supported builds remain idle until capture is selected.
 *
 * @param isEnabled True only when the platform/build supports runtime capture.
 * @author Dmitriy Moroz
 */
fun uiDiagnosticsModule(isEnabled: Boolean) = module {
    if (isEnabled) {
        single<UiDiagnosticTrace> { createUiDiagnosticTrace() }
        single<UiDiagnostics> {
            RecordingUiDiagnostics(
                eventCapacity = 512,
                ownerCapacity = 64,
                trace = get()
            )
        }
    } else {
        single<UiDiagnostics> { NoOpUiDiagnostics }
    }
}
