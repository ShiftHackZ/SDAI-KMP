package com.shifthackz.aisdv1.presentation.components.widget.dialog

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString

/**
 * Displays a localized error dialog with a single acknowledgement action.
 *
 * @param text Error details resolved as UI text during composition.
 * @param onDismissRequest Requests that the caller remove the dialog after acknowledgement or dismissal.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ErrorDialog(
    text: UiText,
    onDismissRequest: () -> Unit = {},
) {
    ErrorDialogContent(
        title = Localization.string("error_title"),
        text = text.asString(),
        okText = Localization.string("ok"),
        onDismissRequest = onDismissRequest,
    )
}
