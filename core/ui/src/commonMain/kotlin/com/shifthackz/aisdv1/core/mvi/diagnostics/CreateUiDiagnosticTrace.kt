package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Creates the platform bridge only from the enabled diagnostics DI module.
 *
 * @return Owning platform implementation; iOS has no platform trace side effects.
 * @author Dmitriy Moroz
 */
internal expect fun createUiDiagnosticTrace(): UiDiagnosticTrace
