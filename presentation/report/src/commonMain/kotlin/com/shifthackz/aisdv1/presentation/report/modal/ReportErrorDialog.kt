package com.shifthackz.aisdv1.presentation.report.modal

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog

/**
 * Shows generic or explicit submission failures; absence of an error emits no dialog.
 *
 * @param error Caller-owned report failure and optional user-facing explanation.
 * @param onDismissRequest Requests clearing the report error without discarding the edited fields.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ReportErrorDialog(error: ErrorState, onDismissRequest: () -> Unit) {
    when (error) {
        ErrorState.None -> Unit
        ErrorState.Generic -> ErrorDialog(
            text = Localization.string("error_generic").asUiText(),
            onDismissRequest = onDismissRequest,
        )

        is ErrorState.WithMessage -> ErrorDialog(
            text = error.message,
            onDismissRequest = onDismissRequest,
        )
    }
}
