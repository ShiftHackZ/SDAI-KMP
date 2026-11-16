package com.shifthackz.aisdv1.presentation.model

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus

/** Converts queued/loading progress to indeterminate UI and valid sampling totals to a bounded step pair.
 * @author Dmitriy Moroz */
internal fun EasyDiffusionProgress.toGenerationModal() = GenerationModal.Generating(
    title = Localization.string("srv_type_easy_diffusion").asUiText(),
    canCancel = true,
    status = if (totalSteps > 0 && step >= 0) LocalDiffusionStatus(
        step.coerceAtMost(totalSteps),
        totalSteps
    ) else null,
)
