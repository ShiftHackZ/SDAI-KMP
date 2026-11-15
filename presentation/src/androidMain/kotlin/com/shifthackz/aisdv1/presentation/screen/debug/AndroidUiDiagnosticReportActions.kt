package com.shifthackz.aisdv1.presentation.screen.debug

import android.content.Context
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticsSnapshot
import com.shifthackz.aisdv1.core.mvi.diagnostics.toExportText
import com.shifthackz.aisdv1.presentation.utils.ReportProblemEmailComposer
import java.io.File
import kotlinx.coroutines.withContext

/**
 * Writes only a user-selected capture and opens the existing report chooser; never sends a report.
 *
 * @param context Application context used for the chooser.
 * @param files Existing FileProvider cache paths.
 * @param dispatchers IO and UI execution boundaries.
 * @param reports Existing problem-report composer.
 * @author Dmitriy Moroz
 */
internal class AndroidUiDiagnosticReportActions(
    private val context: Context,
    private val files: FileProviderDescriptor,
    private val dispatchers: DispatchersProvider,
    private val reports: ReportProblemEmailComposer,
) : UiDiagnosticReportActions {
    override suspend fun share(snapshot: UiDiagnosticsSnapshot) {
        val file = withContext(dispatchers.io) {
            val directory = File(files.logsCacheDirPath).apply { check(exists() || mkdirs()) }
            File(directory, "ui-diagnostics.txt").apply { writeText(snapshot.toExportText()) }
        }
        withContext(dispatchers.ui) { reports.invoke(context, selectedCapture = file) }
    }
}
