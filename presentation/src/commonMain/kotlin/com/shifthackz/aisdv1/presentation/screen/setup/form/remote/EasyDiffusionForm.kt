package com.shifthackz.aisdv1.presentation.screen.setup.form.remote

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.screen.setup.component.message
import com.shifthackz.aisdv1.presentation.screen.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState

/**
 * Anonymous LAN setup with sharing/restart guidance and no unsupported authentication controls.
 * @param state Candidate endpoint and validation state.
 * @param strings Existing localized setup labels and validation messages.
 * @param processIntent Sends candidate edits to the setup reducer.
 * @author Dmitriy Moroz
 */
@Composable
internal fun EasyDiffusionForm(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
) {
    Column {
        FormTitle(
            Localization.string("srv_type_easy_diffusion"),
            Localization.string("easy_diffusion_setup_hint")
        )
        SetupTextField(
            value = state.easyDiffusionUrl,
            onValueChange = { processIntent(ServerSetupIntent.UpdateEasyDiffusionUrl(it)) },
            label = strings.serverUrl,
            keyboardType = KeyboardType.Uri,
            error = state.easyDiffusionUrlValidationError?.message(strings),
        )
    }
}
