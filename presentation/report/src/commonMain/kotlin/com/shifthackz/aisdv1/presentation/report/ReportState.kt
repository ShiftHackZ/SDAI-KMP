package com.shifthackz.aisdv1.presentation.report

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ReportReason
import com.shifthackz.aisdv1.presentation.components.model.ErrorState

/**
 * Editable report content and submission feedback for a selected generation image.
 *
 * @param loading Whether the source image is loading or submission is active.
 * @param error Submission error currently presented by the route.
 * @param imageBase64 Original encoded generation image attached to the report.
 * @param text User-entered report description.
 * @param reason Selected report category.
 * @param reportSent Whether the submission UI should display completion.
 * @author Dmitriy Moroz
 */
data class ReportState(
    val loading: Boolean = true,
    val error: ErrorState = ErrorState.None,
    val imageBase64: String = "",
    val text: String = "",
    val reason: ReportReason = ReportReason.Other,
    val reportSent: Boolean = false,
) : MviState
