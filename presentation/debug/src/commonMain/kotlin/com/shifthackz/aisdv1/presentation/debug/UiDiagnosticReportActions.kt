package com.shifthackz.aisdv1.presentation.debug

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticsSnapshot

/**
 * Explicit capture export through the platform problem report/share flow.
 * Implementations never discover stale capture files or upload diagnostics themselves.
 *
 * @author Dmitriy Moroz
 */
interface UiDiagnosticReportActions {
    /**
     * Creates an attachment only from the selected bounded snapshot and opens the report chooser.
     *
     * @param snapshot Current capture explicitly selected for sharing by the user.
     *
     * @author Dmitriy Moroz
     */
    suspend fun share(snapshot: UiDiagnosticsSnapshot)
}
