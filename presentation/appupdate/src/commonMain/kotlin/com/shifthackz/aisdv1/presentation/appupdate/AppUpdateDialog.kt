package com.shifthackz.aisdv1.presentation.appupdate

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.widget.dialog.DecisionInteractiveDialog

/**
 * Presents a version offer without performing discovery, navigation or dependency resolution.
 *
 * @param version Published stable version or nightly identifier shown in the explanation.
 * @param onUpdate Requests opening the distribution's trusted download destination.
 * @param onDismiss Consumes this offer without changing the installed application.
 * @param languageCode Catalog used for the dialog; defaults to the active application language.
 * @author Dmitriy Moroz
 */
@Composable
internal fun AppUpdateDialog(
    version: String,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    languageCode: String = Localization.currentLanguageCode(),
) {
    DecisionInteractiveDialog(
        title = Localization.string("app_update_title", languageCode = languageCode).asUiText(),
        text = Localization.string("app_update_message", version, languageCode = languageCode)
            .asUiText(),
        confirmActionText = Localization.string("app_update_action", languageCode = languageCode)
            .asUiText(),
        dismissActionText = Localization.string("app_update_later", languageCode = languageCode)
            .asUiText(),
        onConfirmAction = onUpdate,
        onDismissRequest = onDismiss,
    )
}
