@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.txt2img

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.presentation.model.compactDisplayName
import com.shifthackz.aisdv1.presentation.platform.rememberExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.widget.input.GenerationInputFormEvent
import com.shifthackz.aisdv1.presentation.widget.input.generationInputFormItems
import com.shifthackz.aisdv1.presentation.widget.scrollbar.verticalScrollbar


/**
 * Displays the text-to-image card as independently composed list items. Advanced fields below
 * the viewport are not constructed when the user expands options.
 *
 * @param state Current generation inputs, provider and status.
 * @param strings Localized screen labels retained by the screen contract.
 * @param promptChipTextFieldState Caller-owned prompt selection and chip editing state.
 * @param negativePromptChipTextFieldState Caller-owned negative-prompt chip editing state.
 * @param processIntent Receives generation edits and extension actions.
 * @author Dmitriy Moroz
 */
@Composable
internal fun TextToImageBody(
    state: TextToImageState,
    strings: TextToImageStrings,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    processIntent: (TextToImageIntent) -> Unit,
) {
    val listState = rememberLazyListState()
    val urlLauncher = rememberExternalUrlLauncher()
    val onEvent: (GenerationInputFormEvent) -> Unit = remember(processIntent, urlLauncher) {
        { event ->
            if (event == GenerationInputFormEvent.OpenADetailerInstallInstructions) {
                urlLauncher.openUrl(ADETAILER_INSTALL_URL)
            } else event.toTextToImageIntent()?.let(processIntent)
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().verticalScrollbar(listState),
        state = listState,
        contentPadding = PaddingValues(16.dp),
    ) {
        generationInputFormItems(
            state = state,
            isImg2Img = false,
            promptChipTextFieldState = promptChipTextFieldState,
            negativePromptChipTextFieldState = negativePromptChipTextFieldState,
            onEvent = onEvent,
            header = {
                Text(
                    modifier = Modifier.padding(bottom = 14.dp),
                    text = state.mode.compactDisplayName(state.platform),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            footer = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    state.error?.let { message ->
                        Text(
                            modifier = Modifier.padding(top = 14.dp),
                            text = message.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    state.message?.let { message ->
                        Text(
                            modifier = Modifier.padding(top = if (state.error == null) 14.dp else 0.dp),
                            text = message.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (state.generating) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(
                                top = if (state.error == null && state.message == null) 14.dp else 0.dp,
                            ),
                        )
                    }
                }
            },
        )
    }
}

private const val ADETAILER_INSTALL_URL = "https://github.com/Bing-su/adetailer#install"
