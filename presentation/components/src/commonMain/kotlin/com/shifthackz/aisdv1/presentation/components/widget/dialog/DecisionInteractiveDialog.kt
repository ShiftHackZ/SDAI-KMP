package com.shifthackz.aisdv1.presentation.components.widget.dialog

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.core.model.asUiText

/**
 * Presents a caller-owned decision with optional interactive content beneath its explanation.
 *
 * @param title Dialog heading.
 * @param text Explanation of the decision.
 * @param confirmActionText Label of the affirmative action.
 * @param dismissActionText Label of the dismiss/cancel action.
 * @param onConfirmAction Requests the caller's affirmative operation.
 * @param onDismissRequest Requests cancellation or dismissal of the dialog.
 * @param content Additional controls rendered inside the dialog body.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun DecisionInteractiveDialog(
    title: UiText,
    text: UiText,
    confirmActionText: UiText = Localization.string("ok").asUiText(),
    dismissActionText: UiText = Localization.string("cancel").asUiText(),
    onConfirmAction: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    DecisionInteractiveDialogContent(
        title = title.asString(),
        text = text.asString(),
        confirmActionText = confirmActionText.asString(),
        dismissActionText = dismissActionText.asString(),
        onConfirmAction = onConfirmAction,
        onDismissRequest = onDismissRequest,
        content = content,
    )
}
