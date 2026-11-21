package com.shifthackz.aisdv1.presentation.configuration.setup.form.remote

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.component.message
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings

/**
 * Configures a complete image API prefix and optional bearer token.
 *
 * @param state Current independent compatible-server form values.
 * @param strings Localized setup labels and help.
 * @param processIntent Sends field changes to the setup reducer.
 * @param modifier Layout modifiers for the compatible API-prefix and optional-token form.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun OpenAiCompatibleForm(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FormTitle(strings.openAiCompatibleTitle, strings.openAiCompatibleSubtitle)
        SetupTextField(
            value = state.openAiCompatibleUrl,
            onValueChange = { processIntent(ServerSetupIntent.UpdateOpenAiCompatibleUrl(it)) },
            label = strings.openAiCompatibleUrl,
            keyboardType = KeyboardType.Uri,
            error = state.openAiCompatibleUrlValidationError?.message(),
        )
        SetupTextField(
            value = state.openAiCompatibleApiKey,
            onValueChange = { processIntent(ServerSetupIntent.UpdateOpenAiCompatibleApiKey(it)) },
            label = strings.optionalApiKey,
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
        )
    }
}
