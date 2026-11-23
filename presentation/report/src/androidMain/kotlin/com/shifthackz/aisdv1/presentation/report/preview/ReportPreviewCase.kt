package com.shifthackz.aisdv1.presentation.report.preview

import com.shifthackz.aisdv1.presentation.report.ReportState

/**
 * Pairs fixed report data with a readable label for the design surface.
 *
 * @param name Scenario label displayed above the preview.
 * @param state Caller-owned image, draft, submission and error state to illustrate.
 * @author Dmitriy Moroz
 */
internal data class ReportPreviewCase(val name: String, val state: ReportState)
