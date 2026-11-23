package com.shifthackz.aisdv1.presentation.report

import com.shifthackz.aisdv1.core.mvi.MviIntent
import com.shifthackz.aisdv1.domain.entity.ReportReason

/**
 * Editable report fields, submission and navigation requests for one generation result.
 *
 * @author Dmitriy Moroz
 */
sealed interface ReportIntent : MviIntent {
    /**
     * Replaces the report description.
     *
     * @param text User-entered description sent with the report.
     * @author Dmitriy Moroz
     */
    data class UpdateText(val text: String) : ReportIntent

    /**
     * Selects the report category.
     *
     * @param reason Category attached to the report submission.
     * @author Dmitriy Moroz
     */
    data class UpdateReason(val reason: ReportReason) : ReportIntent

    /**
     * Submits the current report description, category and generation image.
     *
     * @author Dmitriy Moroz
     */
    data object Submit : ReportIntent

    /**
     * Returns to the previous route.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBack : ReportIntent

    /**
     * Dismisses the currently displayed submission error.
     *
     * @author Dmitriy Moroz
     */
    data object DismissError : ReportIntent
}
