package com.shifthackz.aisdv1.presentation.debug

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticsSnapshot

/**
 * No export bridge on unsupported targets; capture controls are hidden there.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpUiDiagnosticReportActions : UiDiagnosticReportActions {
    override suspend fun share(snapshot: UiDiagnosticsSnapshot) = Unit
}
