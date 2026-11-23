package com.shifthackz.aisdv1.presentation.report.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ReportReason
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.presentation.report.ReportState

/**
 * Supplies every report category plus loading, editing, sent and submission-error snapshots.
 * Fixed image bytes and inert callbacks keep the previews independent of persisted images and HTTP.
 *
 * @author Dmitriy Moroz
 */
internal class ReportPreviewParameterProvider : PreviewParameterProvider<ReportPreviewCase> {
    private val ready = ReportPreviewFixtures.ready
    private val cases = listOf(
        ReportPreviewCase("Initial loading", ReportState()),
        ReportPreviewCase(
            "Submitting",
            ready.copy(loading = true, text = ReportPreviewFixtures.Description)
        ),
        ReportPreviewCase("Empty description", ready),
        ReportPreviewCase("Image unavailable", ready.copy(imageBase64 = "")),
        ReportPreviewCase(
            "Written description",
            ready.copy(text = ReportPreviewFixtures.Description)
        ),
        ReportPreviewCase(
            "Long description",
            ready.copy(text = ReportPreviewFixtures.longDescription)
        ),
        ReportPreviewCase("Sent confirmation", ready.copy(reportSent = true)),
        ReportPreviewCase(
            "Generic failure",
            ready.copy(text = ReportPreviewFixtures.Description, error = ErrorState.Generic)
        ),
        ReportPreviewCase(
            "Detailed failure", ready.copy(
                text = ReportPreviewFixtures.Description,
                error = ErrorState.WithMessage("The report could not be sent. Please try again.".asUiText()),
            )
        ),
        ReportPreviewCase(
            "Long failure message", ready.copy(
                text = ReportPreviewFixtures.Description,
                error = ErrorState.WithMessage(
                    "The report could not be sent because the connection timed out. Your description is still available. Check the connection and try submitting again.".asUiText(),
                ),
            )
        ),
    ) + ReportReason.entries.map { reason ->
        ReportPreviewCase(
            "Reason · $reason",
            ready.copy(reason = reason, text = ReportPreviewFixtures.Description)
        )
    }

    /**
     * Ordered report states rendered by each light and dark preview annotation.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<ReportPreviewCase> = cases.asSequence()

    /**
     * Identifies the draft, category or submission outcome shown by this preview.
     *
     * @param index Position in the ordered scenario sequence.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
