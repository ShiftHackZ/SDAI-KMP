package com.shifthackz.aisdv1.core.mvi.diagnostics

/**
 * Supplies the platform bridge through the diagnostics composition root.
 *
 * @author Dmitriy Moroz
 */
internal actual fun createUiDiagnosticTrace(): UiDiagnosticTrace = IosUiDiagnosticTrace()
