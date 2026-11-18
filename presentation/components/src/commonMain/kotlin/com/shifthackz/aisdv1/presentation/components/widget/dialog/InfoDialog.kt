package com.shifthackz.aisdv1.presentation.components.widget.dialog

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString

/**
 * Displays an informational message with the localized acknowledgement action.
 *
 * @param title Dialog heading.
 * @param subTitle Explanatory body text.
 * @param onDismissRequest Requests removal after acknowledgement or outside dismissal.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun InfoDialog(
    title: UiText,
    subTitle: UiText,
    onDismissRequest: () -> Unit = {},
) {
    InfoDialogContent(
        title = title.asString(),
        subTitle = subTitle.asString(),
        okText = Localization.string("ok"),
        onDismissRequest = onDismissRequest,
    )
}
